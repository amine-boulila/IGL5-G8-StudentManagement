# AWS Kubernetes deployment

The AWS configuration is separate from the existing local Docker Desktop
deployment in `terraform/`. The AWS pipeline deploys to Amazon EKS only; it
does not use ECS. The manually created cluster `amazing-hiphop-rainbow` remains
untouched. By default Terraform creates a separate cluster named
`student-management` in `us-east-1`, and the pipeline deploys to that new
cluster. The pipeline builds the application image and pushes it to the Docker
Hub `student-management` repository before any AWS stages.

The Terraform configuration creates a VPC, EKS control plane, and one
`t3.medium` managed worker node group. It reuses the existing AWS Academy
control-plane and worker IAM roles instead of creating roles. Worker nodes
run in public subnets to avoid NAT Gateway charges; no SSH access is
configured. The EKS API and application NodePort are restricted to
`AWS_CLUSTER_ADMIN_CIDR`; the Grafana service uses NodePort `30090` and is
restricted to that same CIDR. The EBS CSI add-on is disabled. EKS
control-plane and EC2 resources incur AWS charges.

The standard EKS module was replaced with AWS provider resources because that
module queries the caller's IAM session role with `iam:GetRole`, which AWS
Academy explicitly denies. Terraform does not create or inspect IAM roles in
this configuration; AWS still needs to allow the Academy role to create the
network/EKS resources and pass the two pre-existing roles to EKS.

The pipeline defaults to enabling both `PROVISION_EKS_WITH_TERRAFORM` and
`DEPLOY_TO_EKS`. Terraform runs plan and immediately applies that exact saved
plan with `-auto-approve`. To deploy to the manually created cluster instead,
disable `PROVISION_EKS_WITH_TERRAFORM` and set `EKS_CLUSTER_NAME` to
`amazing-hiphop-rainbow`. Provisioning requires a current, approved public
IPv4 CIDR (typically `address/32`) in `AWS_CLUSTER_ADMIN_CIDR`.

## Jenkins prerequisites

- AWS CLI v2 and Terraform 1.17.0 are installed on the Jenkins executor.
- Docker CLI is installed in Jenkins and its Docker daemon socket is mounted.
  See the Jenkins Docker setup instructions in `JENKINS.md`. Access to that
  socket gives Jenkins control over the host Docker daemon.
- Jenkins has a Docker Hub **Username with password** credential with ID
  `dockerhub-credentials`. The `student-management` Docker Hub repository
  should be public so EKS worker nodes can pull it without an image-pull
  secret.
- Jenkins has a Secret file credential with ID `awscredentials`. It must be
  an AWS shared-credentials INI file containing a `[default]` profile. Keep
  the file private and never print its contents in pipeline logs.
- The AWS credentials must belong to an account/role authorized to describe
  the cluster, obtain an EKS token, and deploy Kubernetes resources. For
  provisioning, the Academy role also needs permission to create the VPC,
  subnets, routes, security-group rule, EKS cluster, managed node group,
  add-ons, access entry, and access-policy association, plus `iam:PassRole`
  for the two pre-existing Academy EKS roles. This configuration does not
  create IAM roles or EBS resources. An Academy explicit deny can still
  prevent one or more of these operations.
- The configured control-plane role must trust `eks.amazonaws.com` and have
  the EKS cluster service policy. The worker role must trust `ec2.amazonaws.com`
  and have `AmazonEKSWorkerNodePolicy`,
  `AmazonEC2ContainerRegistryReadOnly`, and `AmazonEKS_CNI_Policy`.
- Jenkins has a Secret file credential with ID `aws-mysql-env`, containing
  `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and
  `MYSQL_ROOT_PASSWORD` as dotenv-style `KEY=value` lines. Use a non-root
  `MYSQL_USER`; never put this file in Git or pipeline logs.
- Jenkins has a **Secret text** credential with ID
  `grafana-admin-password`; use a strong unique password and do not put it in
  Git.
- Do not use AWS root credentials.
- Ensure the EKS API endpoint access policy admits Jenkins. If using a
  restricted public endpoint, allow Jenkins' stable egress address.

## Run from Jenkins

Run the Jenkins pipeline with `AWS_CLUSTER_ADMIN_CIDR` set to the current
approved public IPv4 CIDR. The defaults create `student-management` and deploy
to it. The ordered stages check out code, clean/build/test, analyze with
SonarQube, publish to Nexus, build and push the Docker Hub image, verify AWS
credentials, provision EKS using Terraform, update kubeconfig, deploy MySQL
and the application, deploy Prometheus and Grafana, then send ten safe GET
requests to the EKS student API so the HTTP request-rate dashboard has sample
traffic. AWS shared credentials are supplied through
`AWS_SHARED_CREDENTIALS_FILE`; credential contents are not printed or copied
into environment variables.

The AWS identity must be authorized to create VPC and EKS resources and pass
the existing IAM roles, then access the resulting EKS cluster for Kubernetes
deployment. Do not broaden the Kubernetes API allowlist to `0.0.0.0/0`.

Terraform state and the `.terraform` working directory are local to the
Jenkins workspace; restrict workspace access, retain the workspace between
runs, and do not archive or commit state files. Losing that state after apply
can leave AWS resources unmanaged.

## Current scope

The deployment uses a single MySQL Deployment with ephemeral `emptyDir`
storage and one Spring Boot replica. **MySQL data is lost if its pod is
recreated or the node is replaced.** Prometheus scrapes Spring Boot Actuator
metrics over an internal ClusterIP service; Prometheus itself is not exposed
outside the cluster. Grafana is exposed at
`http://<node-public-ip>:30090` with login `admin` and the Jenkins-stored
`grafana-admin-password`. Access is restricted by the node security group to
`AWS_CLUSTER_ADMIN_CIDR`. Prometheus metrics use ephemeral storage with a
seven-day retention period.

The application is exposed as NodePort `30089`. Use
`http://<node-public-ip>:30089/student/swagger-ui/index.html` for the
application UI.
Use `kubectl get nodes -o wide` to find a worker's public address.

The Terraform EKS configuration creates a separate `student-management`
cluster. It does not import, adopt, or modify the manually created
`amazing-hiphop-rainbow` cluster.

## Troubleshooting nodes not registering

The pipeline waits up to 10 minutes for Kubernetes node objects to register
after updating kubeconfig before waiting for them to become Ready. If no nodes
register, it prints the node group names, status, desired size, and AWS health
issues. Check that the selected cluster has a managed node group with desired
size greater than zero, that the node role and trust relationship are valid,
and that AWS Academy permits the required EKS/EC2 operations. A cluster with
no worker nodes cannot run the application or Grafana workloads.
