terraform {
  backend "s3" {
    bucket       = "student-management-tfstate-amine-2026"
    key          = "student-management/terraform.tfstate"
    region       = "us-east-1"
    encrypt      = true
    use_lockfile = true
  }
}