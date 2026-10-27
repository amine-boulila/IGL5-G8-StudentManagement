output "cluster_name" {
  description = "Name of the EKS cluster"
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "API endpoint of the EKS cluster"
  value       = aws_eks_cluster.this.endpoint
}

output "region" {
  description = "AWS region of the cluster"
  value       = var.aws_region
}
