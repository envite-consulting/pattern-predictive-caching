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

resource "random_password" "web_user_password" {
  length           = 16
  special          = true
  override_special = "%=?@+#:"
}

locals {
  web_user_username = var.subdomain
  web_user_password = var.web_user_password != null ? var.web_user_password : random_password.web_user_password.result
}

resource "bcrypt_hash" "web_user_password" {
  cleartext = local.web_user_password
  cost      = 10
}

locals {
  web_user_credentials_crypt = "${local.web_user_username}:${bcrypt_hash.web_user_password.id}"
}

output "web_user_credentials" {
  value     = "${local.web_user_username}:${local.web_user_password}"
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
    },
    {
      path        = "/etc/pki/tls/certs/docker.clients.ca.pem"
      content     = tls_self_signed_cert.docker_client_ca.cert_pem
      permissions = "0644"
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
  --tlscert=/etc/pki/tls/certs/${local.fqdn}.pem --tlskey=/etc/pki/tls/private/${local.fqdn}.pem \
  --tlscacert=/etc/pki/tls/certs/docker.clients.ca.pem
EOT
    }
  ]

  traefik_static_config = {
    providers = {
      # https://doc.traefik.io/traefik/providers/file/
      file = {
        directory = "/etc/traefik/dynamic/"
        watch : true
      }
      # https://doc.traefik.io/traefik/providers/docker/
      docker = {
        exposedByDefault : false
        defaultRule : "Host(`{{ normalize .ContainerName }}.${local.fqdn}`)"
      }
    }

    # https://doc.traefik.io/traefik/routing/entrypoints/
    entryPoints = {
      websecure = {
        address = ":443/tcp"
        http2   = {
          maxConcurrentStreams : 250
        }
        http = {
          tls = {}
        }
      }
      traefik = {
        address = ":8080/tcp"
      }
    }

    # https://doc.traefik.io/traefik/operations/api/
    api = {
      dashboard = true
    }
    # https://doc.traefik.io/traefik/operations/ping/
    ping = {
      manualRouting = true
    }
    # https://doc.traefik.io/traefik/observability/metrics/prometheus/
    metrics = {
      prometheus = {
        manualRouting = true
      }
    }
  }

  traefik_dynamic_tls_config = {
    # https://doc.traefik.io/traefik/https/tls/
    tls = {
      certificates = [
        {
          certFile = "/etc/pki/tls/certs/${local.fqdn}.chained.pem"
          keyFile  = "/etc/pki/tls/private/${local.fqdn}.pem"
        }
      ]
      options = {
        default = {
          sniStrict = true
          alpnProtocols : ["http/1.1", "h2"]
        }
      }
    }
  }

  traefik_dynamic_auth_config = {
    http = {
      middlewares = {
        auth-admin = {
          # https://doc.traefik.io/traefik/middlewares/http/basicauth/
          basicAuth = {
            users = [
              local.web_admin_credentials_crypt
            ]
          }
        }
        auth-user = {
          # https://doc.traefik.io/traefik/middlewares/http/basicauth/
          basicAuth = {
            users = [
              local.web_admin_credentials_crypt,
              local.web_user_credentials_crypt
            ]
          }
        }
      }
    }
  }

  traefik_dynamic_routers_config = {
    http = {
      routers = {
        # https://doc.traefik.io/traefik/operations/api/
        websecure-api = {
          entryPoints = ["websecure"]
          rule        = "Host(`traefik.${local.fqdn}`) && (PathPrefix(`/api`) || PathPrefix(`/dashboard`))"
          service     = "api@internal"
          middlewares = ["auth-admin"]
          tls         = {}
        }
        # https://doc.traefik.io/traefik/operations/api/
        traefik-api = {
          entryPoints = ["traefik"]
          rule        = "PathPrefix(`/api`) || PathPrefix(`/dashboard`)"
          service     = "api@internal"
        }
        # https://doc.traefik.io/traefik/operations/ping/
        traefik-ping = {
          entryPoints = ["traefik"]
          rule        = "PathPrefix(`/ping`)"
          service     = "ping@internal"
        }
        # https://doc.traefik.io/traefik/observability/metrics/prometheus/
        traefik-prometheus = {
          entryPoints = ["traefik"]
          rule        = "PathPrefix(`/metrics`)"
          service     = "prometheus@internal"
        }
      }
    }
  }

  traefik_files = [
    {
      path        = "/etc/traefik/traefik.yaml"
      content     = yamlencode(local.traefik_static_config)
      permissions = "0640"
    },
    {
      path        = "/etc/traefik/dynamic/tls.yaml"
      content     = yamlencode(local.traefik_dynamic_tls_config)
      permissions = "0640"
    },
    {
      path        = "/etc/traefik/dynamic/auth.yaml"
      content     = yamlencode(local.traefik_dynamic_auth_config)
      permissions = "0640"
    },
    {
      path        = "/etc/traefik/dynamic/routers.yaml"
      content     = yamlencode(local.traefik_dynamic_routers_config)
      permissions = "0640"
    },
    {
      path    = "/etc/systemd/system/traefik.service"
      content = <<-EOT
[Unit]
Description=Traefik
Documentation=https://doc.traefik.io/traefik/
After=network-online.target
AssertFileIsExecutable=/usr/bin/traefik
AssertPathExists=/etc/traefik/traefik.yaml

[Service]
# Run traefik as its own user
User=traefik
AmbientCapabilities=CAP_NET_BIND_SERVICE

# configure service behavior
Type=notify
ExecStart=/usr/bin/traefik --configFile=/etc/traefik/traefik.yaml
Restart=always
WatchdogSec=1s

# lock down system access
# prohibit any operating system and configuration modification
ProtectSystem=strict
# create separate, new (and empty) /tmp and /var/tmp filesystems
PrivateTmp=true
# make /home directories inaccessible
ProtectHome=true
# turns off access to physical devices (/dev/...)
PrivateDevices=true
# make kernel settings (procfs and sysfs) read-only
ProtectKernelTunables=true
# make cgroups /sys/fs/cgroup read-only
ProtectControlGroups=true

[Install]
WantedBy=multi-user.target
EOT
    }
  ]

  cloud_config = {
    users = concat(
      [
        {
          name   = "traefik"
          groups = "docker"
          shell  = "/bin/false"
          system = true
        },
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

    write_files = concat(local.yum_repo_files, local.certificate_files, local.docker_files, local.traefik_files)

    package_upgrade = true
    packages        = [
      "kernel-ml", "kernel-ml-tools", "perf",
      "msr-tools", "sysfsutils",
      "firewalld",
      "docker-ce", "docker-ce-cli", "containerd.io", "docker-compose-plugin", "docker-buildx-plugin",
      "python3", "pip",
      "vim", "tmux",
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
      ["firewall-cmd", "--zone=public", "--permanent", "--add-port=2376/tcp"],

      ["chown", "root:traefik", "/etc/pki/tls/private/${local.fqdn}.pem"],

      ["systemctl", "enable", "docker"],

      "curl -sL --retry 10 --retry-max-time 120 --retry-connrefused https://github.com/traefik/traefik/releases/download/v2.10.5/traefik_v2.10.5_linux_amd64.tar.gz | tar -xz -C /usr/bin/ traefik",
      ["chown", "-R", "traefik:traefik", "/etc/traefik/"],
      ["systemctl", "enable", "traefik"],

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
