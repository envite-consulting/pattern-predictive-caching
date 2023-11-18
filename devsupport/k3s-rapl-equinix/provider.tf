terraform {
  required_providers {
    equinix = {
      source  = "equinix/equinix"
      version = ">= 1.19"
    }
    acme = {
      source  = "vancluever/acme"
      version = "~> 2.18"
    }
    bcrypt = {
      source  = "viktorradnai/bcrypt"
      version = "~> 0.1"
    }
  }
}

locals {
  default_tags = {
    Repository = "https://github.com/envite-consulting/pattern-predictive-caching.git"
    Project    = "k3s-rapl-equinix"
    FQDN       = local.fqdn
    Terraform  = "true"
  }
}

provider "equinix" {
  auth_token = var.metal_auth_token
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = local.default_tags
  }
}

provider "acme" {
  server_url = "https://acme-v02.api.letsencrypt.org/directory"
}
