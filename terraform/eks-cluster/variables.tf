variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "cluster_name" {
  type    = string
  default = "student-management-eks"
}

variable "node_instance_type" {
  type    = string
  default = "t3.small"
}

variable "desired_nodes" {
  type    = number
  default = 1
}

variable "aws_profile" {
  type        = string
  description = "Optional AWS CLI profile. Leave empty to use the default AWS credentials."
  default     = ""
}

variable "cluster_role_name" {
  type        = string
  description = "Pre-created EKS control-plane role supplied by the lab."
  default     = "c231366a5819305l17873900t1w905418-LabEksClusterRole-FsQWzDfOMxNv"
}

variable "node_role_name" {
  type        = string
  description = "Pre-created EKS node role supplied by the lab."
  default     = "c231366a5819305l17873900t1w905418144-LabEksNodeRole-jBUrCUBejSuO"
}

variable "ebs_csi_role_name" {
  type        = string
  description = "Pre-created role with EBS permissions for the EBS CSI addon."
  default     = "LabRole"
}

variable "enable_ebs_csi" {
  type        = bool
  description = "Enable the EBS CSI addon when a compatible IAM role is available."
  default     = false
}
