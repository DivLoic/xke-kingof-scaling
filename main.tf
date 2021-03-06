provider "google" {
  credentials = "${file(".terraform/gcp-credentials.json")}"
  project     = "cloud-fighter-101"
  region      = "europe-west1"
}


resource "google_container_cluster" "primary" {

  name = "cloud-fighter-kube"
  zone = "europe-west1-b"

  min_master_version = "1.11.2-gke.18"

  initial_node_count = 4
}