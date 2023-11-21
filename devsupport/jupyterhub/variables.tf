##################################
# AWS                            #
##################################

variable "aws_region" {
  default = "eu-north-1"
  type    = string
}

variable "route53_public_main_zone" {
  default     = "codelabs.dev"
  type        = string
  description = "A public hosted zone for this domain must already exists in Route 53."
}

variable "subdomain" {
  default     = "equinix"
  type        = string
  description = "Subdomain which should be created."
}


##################################
# Kubernetes                     #
##################################

variable "jupyterhub_chart_version" {
  default = "3.1.0"
  type = string
}


##################################
# Cognito (AWS)                  #
##################################

variable "allowed_domains" {
  default = ["envite.de"]
  type    = list(string)
}

variable "admin_domains" {
  default = ["envite.de"]
  type    = list(string)
}
