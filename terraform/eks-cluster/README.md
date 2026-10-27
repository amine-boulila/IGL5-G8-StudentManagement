# EKS runbook

This directory creates the AWS infrastructure for Student Management:

- VPC, subnets, route tables, internet gateway, NAT gateway, and Elastic IP
- EKS cluster
- EKS managed node group
- Core EKS addons

AWS resources can incur charges. Destroy the infrastructure when the
demonstration is finished.

## Configuration used for the lab

The working AWS Academy configuration is:

```text
Region:       us-east-1
Cluster:      student-management-eks
Node type:    t3.small
Nodes:        1
Profile:      default
```

The lab provides pre-created IAM roles because the temporary `voclabs` role
cannot create or inspect arbitrary IAM roles. Keep the role names in
`terraform.tfvars` private and do not commit that file.

## Prerequisites

Install and configure:

- AWS CLI
- Terraform
- kubectl

Verify the AWS session before starting:

```powershell
aws sts get-caller-identity --profile default
aws configure get region --profile default
```

The region must be an AWS region such as `us-east-1`, not a display name such
as `United States(N.Virginia)`.

## 1. Create the EKS infrastructure

Run all Terraform commands from this directory:

```powershell
cd C:\MSI\Devops-projet-fac\IGL5-G8-StudentManagement\terraform\eks-cluster
```

Create the ignored local variables file only if it does not exist:

```powershell
Copy-Item terraform.tfvars.example terraform.tfvars
```

Check `terraform.tfvars` and use the role names provided by the lab. The
working configuration has values similar to:

```hcl
aws_region         = "us-east-1"
cluster_name       = "student-management-eks"
node_instance_type = "t3.small"
desired_nodes      = 1
aws_profile        = "default"
cluster_role_name  = "YOUR_PRE_CREATED_CLUSTER_ROLE"
node_role_name     = "YOUR_PRE_CREATED_NODE_ROLE"
ebs_csi_role_name  = "LabRole"
```

Initialize, validate, review, and apply:

```powershell
terraform init
terraform fmt
terraform validate
terraform plan
terraform apply
```

Confirm with `yes` when prompted. The first creation can take several minutes.

Update the local Kubernetes configuration after the cluster is created:

```powershell
aws eks update-kubeconfig `
  --region us-east-1 `
  --name student-management-eks `
  --profile default

kubectl get nodes
```

The node must show `Ready` before deploying the application.

## EBS CSI limitation in AWS Academy

The EBS CSI addon requires an IAM role that can be assumed by an EKS
service account. The pre-created `LabRole` in some Academy accounts does not
have that trust policy, so the addon may remain `DEGRADED` with:

```text
no EC2 IMDS role found
```

Do not repeatedly run `terraform apply` when this happens. Ask the lab
administrator for an EBS CSI role with `AmazonEBSCSIDriverPolicy` and EKS
Pod Identity or IRSA trust.

Without that role, do not use the `gp3` PVC deployment below. Use the
temporary `emptyDir` deployment instead.

The Terraform pipeline disables EBS CSI by default with
`enable_ebs_csi=false`, so it does not wait for this unhealthy addon during a
temporary-storage demonstration. Set it to `true` only after a compatible IAM
role is available.

## 2. Deploy the application to Kubernetes

From the repository root:

```powershell
cd C:\MSI\Devops-projet-fac\IGL5-G8-StudentManagement
```

Create the namespace and database secret. Replace the placeholder with a
real password and never commit it:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic student-management-db `
  --from-literal=DB_PASSWORD=YOUR_MYSQL_PASSWORD `
  --namespace=student-management
```

### Temporary demonstration deployment

Use this when EBS CSI is unavailable:

```powershell
kubectl apply -f k8s/mysql-deployment-ephemeral.yaml
kubectl apply -f k8s/mysql-service.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
```

This uses `emptyDir`; MySQL data is lost if the MySQL pod is deleted or
recreated.

### Persistent deployment

Use this only after the EBS CSI addon is healthy:

```powershell
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
```

Check the rollout:

```powershell
kubectl get pods -n student-management
kubectl rollout status deployment/mysql -n student-management
kubectl rollout status deployment/student-management -n student-management
kubectl get service student-management -n student-management
```

Both pods should show `1/1 Running`. The application service exposes port
`8089`, so wait for its external hostname:

```powershell
kubectl get service student-management `
  --namespace=student-management `
  --watch
```

Test the application. Include port `8089` and the trailing slash:

```powershell
curl.exe -i `
  http://LOAD_BALANCER_HOSTNAME:8089/student/
```

For example, the API endpoint for creating a student is:

```text
POST http://LOAD_BALANCER_HOSTNAME:8089/student/students/createStudent
```

## 3. Jenkins pipeline

The current [`Jenkinsfile`](../../Jenkinsfile) builds and publishes the
application. It currently performs:

1. Git checkout
2. Maven build and tests
3. SonarQube analysis
4. Nexus snapshot publication
5. Docker image build
6. Optional Docker Hub publication

The deployment stages require Jenkins credentials named
`aws-credentials` and `student-management-db-password`. The AWS credential
must be allowed to use EKS, and the Jenkins agent must have `aws`, `kubectl`,
and `terraform` installed. Set `DOCKERHUB_IMAGE` to enable Docker Hub
publication and Kubernetes deployment. Set `APPLY_INFRASTRUCTURE` to
`true` only when the EKS infrastructure needs to be created or changed;
leave it `false` for normal application deployments.

The Kubernetes stage uses the temporary MySQL deployment because the AWS
Academy EBS CSI role is not currently available. Change it to the persistent
manifests only after EBS CSI is healthy.

## 4. Destroy everything created by Terraform

First delete the Kubernetes namespace so the AWS LoadBalancer is removed:

```powershell
cd C:\MSI\Devops-projet-fac\IGL5-G8-StudentManagement
kubectl delete namespace student-management --wait=true
```

Then destroy the AWS infrastructure:

```powershell
cd C:\MSI\Devops-projet-fac\IGL5-G8-StudentManagement\terraform\eks-cluster
terraform plan -destroy
terraform destroy
```

Confirm with `yes`. Terraform removes its managed resources in dependency
order, including:

- EKS addons
- node group and EKS cluster
- NAT gateway and Elastic IP
- private and public subnets
- route tables and associations
- internet gateway
- project VPC

Keep `terraform.tfstate` until `terraform destroy` completes successfully.
It identifies the resources Terraform must remove.

If Terraform was applied by Jenkins, create a separate Jenkins pipeline from
the repository using `Jenkinsfile.destroy`. Set its
`CONFIRM_DESTROY` parameter to `DESTROY`. The destroy job must use the same
Jenkins workspace or a shared Terraform state backend that contains the state
created by the apply job.

## 5. Verify cleanup

Check that the EKS cluster is gone:

```powershell
aws eks list-clusters `
  --region us-east-1 `
  --profile default
```

Check non-default VPCs:

```powershell
aws ec2 describe-vpcs `
  --region us-east-1 `
  --profile default `
  --filters "Name=is-default,Values=false" `
  --query "Vpcs[].{VpcId:VpcId,State:State,Tags:Tags}"
```

Check NAT gateways:

```powershell
aws ec2 describe-nat-gateways `
  --region us-east-1 `
  --profile default `
  --filter Name=state,Values=available,pending,deleting `
  --query "NatGateways[].{Id:NatGatewayId,State:State,VpcId:VpcId}"
```

Do not delete the AWS default VPC manually. A VPC with
`IsDefault: true` and its default subnets belong to the AWS account setup,
not to this Terraform project.

## Files that must not be committed

The following contain local credentials, secrets, or Terraform state:

```text
.env
terraform.tfvars
terraform.tfstate
terraform.tfstate.backup
k8s/secret.yaml
```
