#################################
# Common                        #
#################################

locals {
  host_fqdn = "${var.hostname}.${local.fqdn}"
}

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

locals {
  user_ssh_keys     = jsondecode(data.http.equinix_user_ssh.response_body)
  user_ssh_key_ids  = [for entry in local.user_ssh_keys["ssh_keys"] : entry["id"]]
  user_ssh_key_keys = [for entry in local.user_ssh_keys["ssh_keys"] : trimspace(entry["key"])]

  ssh_authorized_keys = setunion(toset([trimspace(tls_private_key.ssh.public_key_openssh)]), local.user_ssh_key_keys)

  users    = jsondecode(data.http.equinix_users.response_body)
  user_ids = [for entry in local.users.users : entry["id"]]
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
      owner       = "root:root"
      permissions = "0600"
    },
    {
      path    = "/etc/pki/tls/certs/${local.fqdn}.chained.pem"
      content = local.domain_cert_certificate_chain
    },
    {
      path    = "/etc/pki/tls/certs/docker.clients.ca.pem"
      content = tls_self_signed_cert.docker_client_ca.cert_pem
    }
  ]

  docker_files = [
    {
      path    = "/etc/systemd/system/docker.service.d/override.conf"
      content = <<-EOT
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd --containerd=/run/containerd/containerd.sock -H fd:// \
  -H tcp://0.0.0.0:2376 --tlsverify \
  --tlscert=/etc/pki/tls/certs/${local.fqdn}.chained.pem --tlskey=/etc/pki/tls/private/${local.fqdn}.pem \
  --tlscacert=/etc/pki/tls/certs/ca.clients.docker.pem
EOT
    }
  ]

  cloud_config = {
    users = [
      {
        name                = "developer"
        groups              = "wheel,docker"
        sudo                = "ALL=(ALL) NOPASSWD:ALL"
        ssh_authorized_keys = local.ssh_authorized_keys
      }
    ]

    ca_certs = {
      trusted = [
        data.http.letsencrypt_intermediate_cert.response_body,
        data.http.letsencrypt_root_cert.response_body
      ]
    }

    write_files = concat(local.yum_repo_files, local.certificate_files, local.docker_files)

    package_upgrade = true
    packages        = [
      "kernel-ml", "kernel-ml-tools", "perf",
      "msr-tools", "sysfsutils",
      "firewalld",
      "docker-ce", "docker-ce-cli", "containerd.io", "docker-compose-plugin", "docker-buildx-plugin",
      "python3", "pip",
      "vim", "tmux",
      "wget", "telnet", "nc", "openssl",
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
      ["firewall-cmd", "--zone=public", "--permanent", "--add-port=2376/tcp"],

      ["systemctl", "enable", "docker"],

      ["reboot"]
    ]
  }
}

resource "equinix_metal_device" "create" {
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

  tags = [for key, value in local.default_tags : "${key}=${value}"]
}

resource "local_sensitive_file" "host_user_data" {
  content  = "#cloud-config\n${yamlencode(local.cloud_config)}"
  filename = "${path.module}/target/cloud-init/cloud-init.yaml"
}

output "host_fqdn" {
  value = local.host_fqdn
}

output "host_public_ipv4" {
  value = equinix_metal_device.create.access_public_ipv4
}

output "host_root_password" {
  value     = equinix_metal_device.create.root_password
  sensitive = true
}
