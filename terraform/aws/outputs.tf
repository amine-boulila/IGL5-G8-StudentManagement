output "aws_region" {
  description = "AWS region used by the deployment."
  value       = var.aws_region
}

output "cluster_name" {
  description = "EKS cluster name."
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Kubernetes API endpoint. Access is restricted to cluster_admin_cidr."
  value       = module.eks.cluster_endpoint
}
