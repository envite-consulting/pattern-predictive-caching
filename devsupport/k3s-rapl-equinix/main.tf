#################################
# Common                        #
#################################

data "equinix_metal_project" "project" {
  name = var.project
}

#################################
# SSH                           #
#################################

resource "tls_private_key" "ssh" {
  algorithm = "RSA"
  rsa_bits  = 3072
}

resource "local_file" "ssh_public_key" {
  content  = tls_private_key.ssh.public_key_openssh
  filename = "${path.module}/target/ssh/id_${lower(tls_private_key.ssh.algorithm)}_${replace(local.host_fqdn, ".", "-")}.pub"
}

output "project_ssh_public_key_filename" {
  value = local_file.ssh_public_key.filename
}

resource "local_sensitive_file" "ssh_private_key" {
  content  = tls_private_key.ssh.private_key_openssh
  filename = "${path.module}/target/ssh/id_${lower(tls_private_key.ssh.algorithm)}_${replace(local.host_fqdn, ".", "-")}"
}

output "project_ssh_private_key_filename" {
  value = local_sensitive_file.ssh_private_key.filename
}

resource "equinix_metal_project_ssh_key" "ssh" {
  project_id = data.equinix_metal_project.project.id
  name       = local.host_fqdn
  public_key = tls_private_key.ssh.public_key_openssh
}

data "http" "equinix_user_ssh" {
  url = "https://api.equinix.com/metal/v1/ssh-keys"

  request_headers = {
    Accept       = "application/json"
    X-Auth-Token = var.metal_auth_token
  }
}

data "http" "equinix_users" {
  url = "https://api.equinix.com/metal/v1/users"

  request_headers = {
    Accept       = "application/json"
    X-Auth-Token = var.metal_auth_token
  }
}

data "http" "gh_user_ssh" {
  for_each = toset(var.host_users_gh)
  url      = "https://github.com/${each.value}.keys"
}

locals {
  user_ssh_keys     = jsondecode(data.http.equinix_user_ssh.response_body)
  user_ssh_key_ids  = [for entry in local.user_ssh_keys["ssh_keys"] : entry["id"]]
  user_ssh_key_keys = [for entry in local.user_ssh_keys["ssh_keys"] : trimspace(entry["key"])]

  ssh_authorized_keys = setunion(toset([trimspace(tls_private_key.ssh.public_key_openssh)]), local.user_ssh_key_keys)

  users    = jsondecode(data.http.equinix_users.response_body)
  user_ids = [for entry in local.users.users : entry["id"]]

  gh_user_ssh_keys = {
    for user, request in data.http.gh_user_ssh : user =>
    [for line in split("\n", request.response_body) : trimspace(line) if trimspace(line) != ""]
  }
}


#################################
# Passwords                    #
#################################

resource "random_password" "web_admin_password" {
  length           = 16
  special          = true
  override_special = "%=?@+#:"
}

locals {
  web_admin_username = "admin"
  web_admin_password = var.web_admin_password != null ? var.web_admin_password : random_password.web_admin_password.result
}

resource "bcrypt_hash" "web_admin_password" {
  cleartext = local.web_admin_password
  cost      = 10
}

locals {
  web_admin_credentials_crypt = "${local.web_admin_username}:${bcrypt_hash.web_admin_password.id}"
}

output "web_admin_credentials" {
  value     = "${local.web_admin_username}:${local.web_admin_password}"
  sensitive = true
}


#################################
# Create Node                   #
#################################

locals {
  // The Equinix AlmaLinux image does not load the 'yum_repos' module, and therefore does not add the repos.
  // We have to do it separately.
  yum_repos = {
    elrepo = {
      name     = "ELRepo.org Community Enterprise Linux Repository - el$releasever"
      enabled  = true
      baseurl  = "https://elrepo.org/linux/elrepo/el$releasever/$basearch/"
      gpgkey   = "https://www.elrepo.org/RPM-GPG-KEY-elrepo.org"
      gpgcheck = true
    }
    elrepo-kernel = {
      name     = "ELRepo.org Community Enterprise Linux Kernel Repository - el$releasever"
      enabled  = true
      baseurl  = "https://elrepo.org/linux/kernel/el$releasever/$basearch/"
      gpgkey   = "https://www.elrepo.org/RPM-GPG-KEY-elrepo.org"
      gpgcheck = true
    }
    epel = {
      name     = "Extra Packages for Enterprise Linux $releasever - $basearch"
      enabled  = true
      baseurl  = "https://dl.fedoraproject.org/pub/epel/$releasever/Everything/$basearch/"
      gpgkey   = "https://dl.fedoraproject.org/pub/epel/RPM-GPG-KEY-EPEL-$releasever"
      gpgcheck = true
    }
    epel-next = {
      name     = "Extra Packages for Enterprise Linux $releasever - Next - $basearch"
      enabled  = true
      baseurl  = "https://dl.fedoraproject.org/pub/epel/next/$releasever/Everything/$basearch/"
      gpgkey   = "https://dl.fedoraproject.org/pub/epel/RPM-GPG-KEY-EPEL-$releasever"
      gpgcheck = true
    }
    docker-ce = {
      name     = "Docker CE Stable - $basearch"
      enabled  = true
      baseurl  = "https://download.docker.com/linux/centos/$releasever/$basearch/stable"
      gpgkey   = "https://download.docker.com/linux/centos/gpg"
      gpgcheck = true
    }
  }
  yum_repo_files = [
    for name, yum_repo in local.yum_repos : {
      path    = "/etc/yum.repos.d/${name}.repo"
      content = <<-EOT
[${name}]
%{ for key, value in yum_repo ~}
${key}=${trimspace(value)}
%{ endfor ~}
EOT
    }
  ]

  certificate_files = [
    {
      path        = "/etc/pki/tls/private/${local.fqdn}.pem"
      content     = acme_certificate.domain_cert.private_key_pem
      permissions = "0640"
    },
    {
      path        = "/etc/pki/tls/certs/${local.fqdn}.pem"
      content     = acme_certificate.domain_cert.certificate_pem
      permissions = "0644"
    },
    {
      path        = "/etc/pki/tls/certs/${local.fqdn}.chained.pem"
      content     = local.domain_cert_certificate_chain
      permissions = "0644"
    },
    {
      path        = "/etc/pki/tls/certs/${local.fqdn}.ca.pem"
      content     = local.domain_cert_ca
      permissions = "0644"
    }
  ]

  traefik_manifest_files = [
    {
      path    = "/var/lib/rancher/k3s/server/manifests/traefik-tls.yaml"
      content = <<-EOT
---
apiVersion: traefik.io/v1alpha1
kind: TLSStore
metadata:
  name: default
  namespace: kube-system
spec:
  defaultCertificate:
    secretName: default-traefik-cert
---
apiVersion: v1
kind: Secret
metadata:
  name: default-traefik-cert
  namespace: kube-system
type: Opaque
data:
  tls.crt: ${base64encode(local.domain_cert_certificate_chain)}
  tls.key: ${base64encode(acme_certificate.domain_cert.private_key_pem)}
EOT
    },
    {
      path    = "/var/lib/rancher/k3s/server/manifests/traefik-dashboard.yaml"
      content = <<-EOT
---
apiVersion: v1
kind: Service
metadata:
  name: traefik-dashboard-external
  namespace: kube-system
spec:
  type: ClusterIP
  ports:
    - name: http
      port: 80
      targetPort: traefik
  selector:
    app.kubernetes.io/instance: traefik-kube-system
    app.kubernetes.io/name: traefik
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: websecure
    traefik.ingress.kubernetes.io/router.middlewares: kube-system-auth-admin@kubernetescrd
  name: traefik-dashboard-external
  namespace: kube-system
spec:
  rules:
    - host: traefik.${local.fqdn}
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: traefik-dashboard-external
                port:
                  name: http
EOT
    },
    {
      path    = "/var/lib/rancher/k3s/server/manifests/traefik-middleware.yaml"
      content = <<-EOT
---
apiVersion: traefik.io/v1alpha1
kind: Middleware
metadata:
  name: auth-admin
  namespace: kube-system
spec:
  basicAuth:
    secret: traefik-auth-admin
---
apiVersion: v1
kind: Secret
metadata:
  name: traefik-auth-admin
  namespace: kube-system
type: Opaque
data:
  users: ${base64encode(local.web_admin_credentials_crypt)}
EOT
    }
  ]

  # https://docs.k3s.io/installation/configuration#putting-it-all-together
  # https://docs.k3s.io/cli/server#k3s-server-cli-help
  k3s_install_cmd = <<EOT
    API_IP=$(curl -fsSL https://metadata.platformequinix.com/metadata | jq -r '.network.addresses | map(select(.public==true and .management==true)) | first | .address')
    bash -c
    '
      curl -sfL --retry 10 --retry-max-time 120 --retry-connrefused https://get.k3s.io |
      K3S_KUBECONFIG_MODE="644" INSTALL_K3S_CHANNEL="${var.k3s_channel}" INSTALL_K3S_EXEC="server"
      sh -s -
        --bind-address $${API_IP}
        --advertise-address $${API_IP}
        --node-name ${var.hostname}
        --node-ip $${API_IP}
        --tls-san $${API_IP} --tls-san ${local.host_fqdn}
        --node-label topology.kubernetes.io/zone=${var.metro}
        --docker
    '
  EOT

  cloud_config = {
    users = concat(
      [
        {
          name                = "developer"
          groups              = "wheel,docker"
          sudo                = "ALL=(ALL) NOPASSWD:ALL"
          ssh_authorized_keys = local.ssh_authorized_keys
        }
      ],
      [
        for user, ssh_authorized_keys in local.gh_user_ssh_keys :
        {
          name                = user
          groups              = "wheel,docker"
          sudo                = "ALL=(ALL) NOPASSWD:ALL"
          ssh_authorized_keys = setunion(ssh_authorized_keys, local.ssh_authorized_keys)
        }
      ]
    )

    ca_certs = {
      trusted = [
        data.http.letsencrypt_intermediate_cert.response_body,
        data.http.letsencrypt_root_cert.response_body
      ]
    }

    write_files = concat(local.yum_repo_files, local.certificate_files, local.traefik_manifest_files, local.external_dns_manifest_files)

    package_upgrade = true
    packages        = [
      "kernel-ml", "kernel-ml-tools", "perf",
      "msr-tools", "sysfsutils",
      "firewalld", "bind-utils",
      "docker-ce", "docker-ce-cli", "containerd.io", "docker-compose-plugin", "docker-buildx-plugin", "fuse-overlayfs",
      "python3", "pip",
      "vim", "tmux", "jq",
      "wget", "telnet", "nc", "openssl", "ca-certificates",
      "gnupg",
      "htop", "btop", "glances",
      "stress-ng"
    ]

    runcmd = [
      ["ln", "-s", "/home/", "/var/home"],

      ["systemctl", "enable", "firewalld"],
      ["systemctl", "start", "firewalld"],
      ["firewall-cmd", "--zone=public", "--permanent", "--remove-service=cockpit"],
      ["firewall-cmd", "--zone=public", "--permanent", "--remove-service=dhcpv6-client"],
      ["firewall-cmd", "--zone=public", "--permanent", "--add-service=ssh"],
      ["firewall-cmd", "--zone=public", "--permanent", "--add-service=https"],

      ["firewall-cmd", "--zone=public", "--permanent", "--add-port=6443/tcp"], # apiserver
      ["firewall-cmd", "--zone=trusted", "--permanent", "--add-source=10.42.0.0/16"], # pods
      ["firewall-cmd", "--zone=trusted", "--permanent", "--add-source=10.43.0.0/16"], # services

      ["systemctl", "enable", "docker"],

      join("", [for line in split("\n", local.k3s_install_cmd) : "${trimspace(line)} " if trimspace(line) != ""]),
      ["systemctl", "enable", "k3s"],

      ["reboot"]
    ]
  }
}

resource "equinix_metal_device" "host" {
  count = var.host_enabled ? 1 : 0

  project_id          = data.equinix_metal_project.project.id
  project_ssh_key_ids = [equinix_metal_project_ssh_key.ssh.id]
  user_ssh_key_ids    = local.user_ids

  billing_cycle = "hourly"

  metro = var.metro
  plan  = var.plan

  operating_system = "alma_9"
  user_data        = "#cloud-config\n${yamlencode(local.cloud_config)}"
  hostname         = local.host_fqdn

  termination_time = timeadd(timestamp(), var.terminate_in)

  lifecycle {
    ignore_changes = [user_data]
  }

  tags = [for key, value in local.default_tags : "${key}=${value}"]
}

resource "local_sensitive_file" "host_user_data" {
  content  = "#cloud-config\n${yamlencode(local.cloud_config)}"
  filename = "${path.module}/target/cloud-init/cloud-init.yaml"
}

output "host_fqdn" {
  value = var.host_enabled ? local.host_fqdn : null
}

output "host_public_ipv4" {
  value = var.host_enabled ? equinix_metal_device.host[0].access_public_ipv4 : null
}

output "host_root_password" {
  value     = var.host_enabled ? equinix_metal_device.host[0].root_password : null
  sensitive = true
}
