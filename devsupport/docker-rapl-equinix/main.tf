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
  user_ssh_key_keys = [for entry in local.user_ssh_keys["ssh_keys"] : entry["key"]]

  ssh_authorized_keys = setunion(toset([tls_private_key.ssh.public_key_openssh]), local.user_ssh_key_keys)

  users    = jsondecode(data.http.equinix_users.response_body)
  user_ids = [for entry in local.users.users : entry["id"]]
}

#################################
# Create Node                   #
#################################

resource "equinix_metal_device" "create" {
  project_id          = data.equinix_metal_project.project.id
  project_ssh_key_ids = [equinix_metal_project_ssh_key.ssh.id]
  user_ssh_key_ids    = local.user_ids

  billing_cycle       = "hourly"

  metro               = var.metro
  plan                = var.plan

  operating_system    = var.operating_system
  hostname            = var.app

  termination_time    = timeadd(timestamp(), "1h")

  tags = [
    "project=${var.project}",
    "app=${var.app}",
    "terraform=true"
  ]
}

output "root_password" {
  value = equinix_metal_device.create.root_password
  sensitive = true
}

output "access_public_ipv4" {
  value = equinix_metal_device.create.access_public_ipv4
}

output "sos_hostname" {
  value = equinix_metal_device.create.sos_hostname
}