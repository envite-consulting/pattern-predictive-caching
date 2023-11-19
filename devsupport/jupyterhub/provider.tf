locals {
  default_tags = {
    Repository = "https://github.com/envite-consulting/pattern-predictive-caching.git"
    Project    = "jupyterhub"
    FQDN       = local.fqdn
    Terraform  = "true"
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = local.default_tags
  }
}

provider "kubernetes" {}

provider "helm" {}
