output "cluster_name" {
  description = "EKS cluster name for kubectl configuration."
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "Public Kubernetes API endpoint."
  value       = aws_eks_cluster.this.endpoint
}

output "vpc_id" {
  description = "ID of the EKS VPC."
  value       = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets used by EKS."
  value       = aws_subnet.public[*].id
}
