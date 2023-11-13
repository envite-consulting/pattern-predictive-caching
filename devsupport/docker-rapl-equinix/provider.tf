terraform {
  required_providers {
    equinix = {
      source  = "equinix/equinix"
      version = ">= 1.19.0"
    }
  }
}

provider "equinix" {
  auth_token = var.metal_auth_token
}
