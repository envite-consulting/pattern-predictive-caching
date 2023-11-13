variable "metal_auth_token" {
  type = string
}

variable "project" {
  default = "default"
  type    = string
}

variable "app" {
  default = "rapl-test"
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

variable "operating_system" {
  default = "alma_9"
  type = string
}