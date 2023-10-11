variable "project" {
  type = string
}

variable "region" {
  type = string
  default = "europe-north1"
}

variable "environment" {
  type = string
}

variable "ip_cidr_range" {
  type = string
  default = "10.0.1.0/24"
}

variable "machine_type" {
  type = string
  default = "e2-standard-2"
}

variable "image_ubuntu_family" {
  type = string
  default = "ubuntu-2204-lts"
  # gcloud compute images list --filter ubuntu-os-cloud
}