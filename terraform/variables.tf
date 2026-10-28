variable "mysql_database" {
  description = "Database created by the MySQL container."
  type        = string
  sensitive   = true
}

variable "mysql_user" {
  description = "Non-root application account created by MySQL."
  type        = string
  sensitive   = true
}

variable "mysql_password" {
  description = "Password for the application database account."
  type        = string
  sensitive   = true
}

variable "mysql_root_password" {
  description = "Password for the MySQL root account."
  type        = string
  sensitive   = true
}

variable "app_image" {
  description = "Application image already available to Docker Desktop Kubernetes."
  type        = string
  default     = "student-management-app-app:latest"
}
