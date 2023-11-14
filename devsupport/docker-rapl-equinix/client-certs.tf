#################################
# Docker Client Certs for mTLS  #
#################################

resource "tls_private_key" "docker_client_ca_key" {
  algorithm = "RSA"
  rsa_bits  = 3072
}

resource "tls_self_signed_cert" "docker_client_ca" {
  private_key_pem = tls_private_key.docker_client_ca_key.private_key_pem

  subject {
    common_name  = "ca.clients.docker"
    organization = local.host_fqdn
  }

  validity_period_hours = 8760

  is_ca_certificate = true

  allowed_uses = ["cert_signing"]
}

resource "tls_private_key" "docker_client_cert_key" {
  algorithm = "RSA"
  rsa_bits  = 3072
}

resource "tls_cert_request" "docker_client_cert_request" {
  private_key_pem = tls_private_key.docker_client_cert_key.private_key_pem

  subject {
    common_name  = "client"
    organization = local.host_fqdn
  }
}

resource "tls_locally_signed_cert" "docker_client_cert" {
  cert_request_pem   = tls_cert_request.docker_client_cert_request.cert_request_pem
  ca_private_key_pem = tls_private_key.docker_client_ca_key.private_key_pem
  ca_cert_pem        = tls_self_signed_cert.docker_client_ca.cert_pem

  validity_period_hours = 8760

  allowed_uses = ["client_auth"]
}

resource "local_file" "docker_domain_ca" {
  content  = local.domain_cert_ca
  filename = "${path.module}/target/certs/docker-client/ca"
}

resource "local_sensitive_file" "docker_client_private_key" {
  content  = tls_private_key.docker_client_cert_key.private_key_pem
  filename = "${path.module}/target/certs/docker-client/key"
}

resource "local_file" "docker_client_cert" {
  content  = tls_locally_signed_cert.docker_client_cert.ca_cert_pem
  filename = "${path.module}/target/certs/docker-client/cert"
}

output "docker_cert_path" {
  value = "${path.module}/target/certs/docker-client"
}
