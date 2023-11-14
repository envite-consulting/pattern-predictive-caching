terraform {
  required_providers {
    equinix = {
      source  = "equinix/equinix"
      version = ">= 1.19.0"
    }
    acme = {
      source  = "vancluever/acme"
      version = "~> 2.18"
    }

  }
}

locals {
  default_tags = {
    Repository = "https://github.com/envite-consulting/pattern-predictive-caching.git"
    Project    = "docker-rapl-equinix"
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
