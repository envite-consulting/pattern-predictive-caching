#################################
# Route53                       #
#################################

locals {
  fqdn = "${var.subdomain}.${var.route53_public_main_zone}"
}

data "aws_route53_zone" "main" {
  name = var.route53_public_main_zone
}

resource "aws_route53_zone" "public" {
  name = local.fqdn
}

resource "aws_route53_record" "public_ns" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = aws_route53_zone.public.name
  type    = "NS"
  ttl     = "300"
  records = aws_route53_zone.public.name_servers
}

resource "aws_route53_record" "public_wildcard" {
  count = var.host_enabled ? 1 : 0

  zone_id = aws_route53_zone.public.zone_id
  name    = "*.${aws_route53_zone.public.name}"
  type    = "A"
  ttl     = "300"
  records = [equinix_metal_device.host[0].access_public_ipv4]
}


#####################################
# ACME Certificate (Let's Encrypt)  #
#####################################

resource "tls_private_key" "domain_cert" {
  algorithm = "RSA"
  rsa_bits  = 3072
}

resource "acme_registration" "domain_cert" {
  account_key_pem = tls_private_key.domain_cert.private_key_pem
  email_address   = var.acme_email
}

resource "acme_certificate" "domain_cert" {
  account_key_pem           = acme_registration.domain_cert.account_key_pem
  common_name               = "*.${aws_route53_zone.public.name}"
  subject_alternative_names = ["*.${aws_route53_zone.public.name}"]

  min_days_remaining = 60

  dns_challenge {
    provider = "route53"
  }

  depends_on = [aws_route53_zone.public, aws_route53_record.public_ns]
}

data "http" "letsencrypt_intermediate_cert" {
  url = "https://letsencrypt.org/certs/lets-encrypt-r3.pem"
}

data "http" "letsencrypt_root_cert" {
  url = "https://letsencrypt.org/certs/isrgrootx1.pem"
}

locals {
  domain_cert_ca                = "${data.http.letsencrypt_intermediate_cert.response_body}${data.http.letsencrypt_root_cert.response_body}"
  domain_cert_certificate_chain = "${acme_certificate.domain_cert.certificate_pem}${local.domain_cert_ca}"
}
