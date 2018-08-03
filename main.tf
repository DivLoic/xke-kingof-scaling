provider "google" {
  credentials = "${file(".terraform/gcp-credentials.json")}"
  project     = "cloud-fighter-101"
  region      = "europe-west1"
}


resource "google_container_cluster" "primary" {

  name = "cloud-fighter-kube"
  zone = "europe-west1-b"

  min_master_version = "1.10.7-gke.6"

  initial_node_count = 4
}