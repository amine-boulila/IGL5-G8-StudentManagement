# Manage the local Kubernetes deployment with Terraform

This configuration creates an isolated deployment in the
`student-management-terraform` namespace. It does not import, update, or delete
the existing `student-management` namespace. Both deployments use the local
Docker Desktop Kubernetes cluster and the same locally built application image.

Terraform manages the namespace, application ConfigMap and Secret, MySQL
StatefulSet and storage, application Deployment, and the MySQL and application
Services.

## Prerequisites

- Docker Desktop Kubernetes is running; `kubectl config current-context`
  reports `docker-desktop`.
- Terraform 1.6 or later is installed and available in PowerShell.
- The application image is available locally:

  ```powershell
  docker compose build app
  ```

- The project root has a private `.env` containing `MYSQL_DATABASE`,
  `MYSQL_USER`, `MYSQL_PASSWORD`, and `MYSQL_ROOT_PASSWORD`.
  `MYSQL_USER` must not be `root`.

## Deploy

From the project root:

```powershell
.\terraform\deploy.ps1
```

The script loads the four database settings into process-scoped Terraform
variables without printing them, initializes and validates the configuration,
creates a saved plan, applies it, then removes the plan and environment
variables. Terraform marks the credential variables sensitive in its output.

Terraform's local state still contains the Kubernetes Secret values in
plaintext. Keep `terraform\terraform.tfstate` private, never commit or share
it, and do not use this local state setup for production. The repository
ignores the local state, provider directory, plan files, and variable files.

The application is exposed at
[http://localhost:30090/student](http://localhost:30090/student), and Swagger
UI is at
[http://localhost:30090/student/swagger-ui/index.html](http://localhost:30090/student/swagger-ui/index.html).

## Inspect and destroy

```powershell
terraform -chdir=terraform state list
terraform -chdir=terraform plan
```

Terraform state contains credentials; keep command output and state files
private. To remove only Terraform-managed resources:

```powershell
.\terraform\deploy.ps1 -Destroy
```

The destroy removes the Terraform namespace and its MySQL PersistentVolumeClaim
and data. It leaves the separate `student-management` namespace deployed by
the Kubernetes manifests untouched. Terraform asks for confirmation before
destroying resources.
