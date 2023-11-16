##################################
# Host (Equinix)                 #
##################################

variable "metal_auth_token" {
  type = string
}

variable "host_enabled" {
  default = true
  type = bool
}

variable "project" {
  default = "default"
  type    = string
}

variable "hostname" {
  default = "docker"
  type    = string
}

variable "metro" {
  default = "sk"
  type    = string
}

variable "plan" {
  default = "m3.small.x86"
  type    = string
}

variable "terminate_in" {
  default = "1h"
  type    = string
}

variable "host_users_gh" {
  default = []
  type = list(string)
}


##################################
# Domain (Let's Encrypt and AWS) #
##################################

variable "acme_email" {
  type = string
}

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
  default = "equinix"
  type = string
  description = "Subdomain which should be created."
}

variable "web_admin_password" {
  default = null
  type = string
}

variable "web_user_password" {
  default = null
  type = string
}
