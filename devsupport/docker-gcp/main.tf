# Create a single Compute Engine instance
resource "google_compute_instance" "default" {
  name         = "${var.environment}-dev-docker-host"
  machine_type = var.machine_type
  zone         = "${var.region}-a"
  tags         = ["ssh"]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/${var.image_ubuntu_family}"
    }
  }

  metadata_startup_script = <<-EOF
    sudo apt-get update
    sudo apt-get upgrade -y

    sudo apt-get install -y \
      ca-certificates curl gnupg lsb-release \
      btop

    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin docker-buildx-plugin

    sudo ln -s /home/ /var/home
    EOF

  network_interface {
    subnetwork = google_compute_subnetwork.default.id

    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}

output "vm_name" {
  value = google_compute_instance.default.name
}

output "vm_ip" {
  value = google_compute_instance.default.network_interface.0.access_config.0.nat_ip
}