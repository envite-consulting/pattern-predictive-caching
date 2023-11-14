variable "metal_auth_token" {
  type = string
}

variable "project" {
  default = "default"
  type    = string
}

variable "app" {
  default = "docker-rapl"
  type = string
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
  default = "2h"
  type = string
}