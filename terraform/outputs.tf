output "application_url" {
  description = "Local URL for the Terraform-managed application."
  value       = "http://localhost:30090/student"
}

output "namespace" {
  description = "Kubernetes namespace managed by this Terraform configuration."
  value       = kubernetes_namespace_v1.student_management.metadata[0].name
}
