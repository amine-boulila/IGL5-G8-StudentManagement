variable "region" {
  description = "Region AWS du Learner Lab"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Nom du cluster EKS"
  type        = string
  default     = "student-management-eks"
}

variable "node_instance_type" {
  description = "Type d'instance EC2 des noeuds"
  type        = string
  default     = "t3.medium"
}

variable "node_count" {
  description = "Nombre de noeuds souhaites"
  type        = number
  default     = 2
}
