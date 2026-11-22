terraform {
  required_version = ">= 1.5.0"
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0.2"
    }
  }
}

provider "docker" {
  host = "npipe:////./pipe/docker_engine"
}

# ------------------------------------------------------------------------------
# Bridge Network Definition
# ------------------------------------------------------------------------------
resource "docker_network" "auditrail_network" {
  name   = "auditrail-network"
  driver = "bridge"
}

# ------------------------------------------------------------------------------
# Persistent Volumes
# ------------------------------------------------------------------------------
resource "docker_volume" "mysql_data" {
  name = "auditrail-mysql-data"
}

resource "docker_volume" "minio_data" {
  name = "auditrail-minio-data"
}

# ------------------------------------------------------------------------------
# Database Container (MySQL 8.0)
# ------------------------------------------------------------------------------
resource "docker_image" "mysql_image" {
  name         = "mysql:8.0.36"
  keep_locally = true
}

resource "docker_container" "mysql" {
  name  = "auditrail-mysql"
  image = docker_image.mysql_image.image_id
  restart = "unless-stopped"

  networks_advanced {
    name = docker_network.auditrail_network.name
  }

  env = [
    "MYSQL_ROOT_PASSWORD=root_secret_password_2026!",
    "MYSQL_DATABASE=auditrail",
    "MYSQL_USER=auditrail_app",
    "MYSQL_PASSWORD=auditrail_secret"
  ]

  ports {
    internal = 3306
    external = 3306
  }

  volumes {
    volume_name    = docker_volume.mysql_data.name
    container_path = "/var/lib/mysql"
  }
}

# ------------------------------------------------------------------------------
# WORM Storage Container (MinIO with Object Lock)
# ------------------------------------------------------------------------------
resource "docker_image" "minio_image" {
  name         = "minio/minio:RELEASE.2024-05-10T01-41-38Z"
  keep_locally = true
}

resource "docker_container" "minio" {
  name    = "auditrail-minio"
  image   = docker_image.minio_image.image_id
  restart = "unless-stopped"
  command = ["server", "/data", "--console-address", ":9001"]

  networks_advanced {
    name = docker_network.auditrail_network.name
  }

  env = [
    "MINIO_ROOT_USER=minioadmin",
    "MINIO_ROOT_PASSWORD=minioadminpassword2026!"
  ]

  ports {
    internal = 9000
    external = 9000
  }

  ports {
    internal = 9001
    external = 9001
  }

  volumes {
    volume_name    = docker_volume.minio_data.name
    container_path = "/data"
  }
}
