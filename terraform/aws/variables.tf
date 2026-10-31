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

variable "cluster_version" {
  description = "Kubernetes version for the EKS cluster."
  type        = string
  default     = "1.36"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs for the EKS cluster and worker nodes."
  type        = list(string)
  default     = ["10.42.0.0/20", "10.42.16.0/20", "10.42.32.0/20"]
}

variable "cluster_role_arn" {
  description = "Existing AWS Academy IAM role ARN trusted by eks.amazonaws.com."
  type        = string
  default     = "arn:aws:iam::423221818337:role/c231366a5819305l17994767t1w423221-LabEksClusterRole-IRzRaF0brgPl"

  validation {
    condition     = startswith(var.cluster_role_arn, "arn:aws:iam::")
    error_message = "cluster_role_arn must be an IAM role ARN."
  }
}

variable "node_role_arn" {
  description = "Existing AWS Academy IAM role ARN trusted by ec2.amazonaws.com and attached to the EKS worker policies."
  type        = string
  default     = "arn:aws:iam::423221818337:role/c231366a5819305l17994767t1w423221818-LabEksNodeRole-OwrwFYwwZaod"

  validation {
    condition     = startswith(var.node_role_arn, "arn:aws:iam::")
    error_message = "node_role_arn must be an IAM role ARN."
  }
}

variable "cluster_admin_role_arn" {
  description = "Existing IAM role granted Kubernetes cluster-admin access through an EKS access entry."
  type        = string
  default     = "arn:aws:iam::423221818337:role/voclabs"

  validation {
    condition     = startswith(var.cluster_admin_role_arn, "arn:aws:iam::")
    error_message = "cluster_admin_role_arn must be an IAM role ARN."
  }
}
