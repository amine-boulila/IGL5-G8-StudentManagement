variable "aws_region" {
  description = "AWS region for the EKS cluster."
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Name used for the AWS VPC and EKS cluster."
  type        = string
  default     = "student-management"
}

variable "cluster_admin_cidr" {
  description = "Public IPv4 CIDR allowed to access the Kubernetes API, such as a fixed address with /32."
  type        = string

  validation {
    condition     = can(cidrhost(var.cluster_admin_cidr, 0)) && !strcontains(var.cluster_admin_cidr, ":")
    error_message = "cluster_admin_cidr must be a valid IPv4 CIDR, for example 203.0.113.25/32."
  }
}
