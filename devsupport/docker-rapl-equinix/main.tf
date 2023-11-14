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

/*
resource "local_file" "ssh_public_key" {
  content  = tls_private_key.ssh.public_key_openssh
  filename = "${path.module}/target/id_${lower(tls_private_key.ssh.algorithm)}_${var.project}-${var.app}.pub"
}

resource "local_sensitive_file" "ssh_private_key" {
  content  = tls_private_key.ssh.private_key_openssh
  filename = "${path.module}/target/id_${lower(tls_private_key.ssh.algorithm)}_${var.project}-${var.app}"
}
*/

resource "equinix_metal_project_ssh_key" "ssh" {
  project_id = data.equinix_metal_project.project.id
  name       = "${var.project}-${var.app}"
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

  cloud_config = {
    users = [
      {
        name                = "developer"
        groups              = "wheel,docker"
        sudo                = "ALL=(ALL) NOPASSWD:ALL"
        ssh_authorized_keys = local.ssh_authorized_keys
      }
    ]

    write_files = [for name, yum_repo in local.yum_repos : {
      path = "/etc/yum.repos.d/${name}.repo"
      content = <<-EOT
[${name}]
%{ for key, value in yum_repo ~}
${key}=${trimspace(value)}
%{ endfor ~}
EOT
    }]

    package_upgrade = true
    packages        = [
      "kernel-ml", "kernel-ml-tools", "perf",
      "msr-tools", "sysfsutils",
      "docker-ce", "docker-ce-cli", "containerd.io", "docker-compose-plugin", "docker-buildx-plugin",
      "python3", "pip",
      "vim", "tmux", "wget", "telnet", "nc", "gnupg",
      "htop", "btop", "glances",
      "stress-ng"
    ]

    runcmd = [
      ["ln", "-s", "/home/", "/var/home"],
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
  hostname         = var.app

  termination_time = timeadd(timestamp(), var.terminate_in)

  tags = [
    "project=${var.project}",
    "app=${var.app}",
    "terraform=true"
  ]
}

output "root_password" {
  value     = equinix_metal_device.create.root_password
  sensitive = true
}

output "access_public_ipv4" {
  value = equinix_metal_device.create.access_public_ipv4
}

output "user_data" {
  value = "#cloud-config\n${yamlencode(local.cloud_config)}"
}