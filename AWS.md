# AWS Kubernetes deployment

The AWS configuration is separate from the existing local Docker Desktop
deployment in `terraform/`. The AWS pipeline deploys to Amazon EKS only; it
does not use ECS. The manually created cluster `amazing-hiphop-rainbow` remains
untouched. By default Terraform creates a separate cluster named
`student-management` in `us-east-1`, and the pipeline deploys to that new
cluster. The pipeline builds the application image and pushes it to the Docker
Hub `student-management` repository before any AWS stages.

The Terraform configuration creates a VPC, EKS control plane, one `t3.medium`
worker node, and the EBS CSI driver. Worker nodes run in public subnets to
avoid NAT Gateway charges; no SSH access is configured. The EKS API and
application NodePort are restricted to `AWS_CLUSTER_ADMIN_CIDR`. EKS
control-plane, EC2, and EBS storage incur AWS charges.

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
  the cluster, obtain an EKS token, and deploy Kubernetes resources. Terraform
  provisioning additionally needs permissions to create VPC, IAM, EKS, and
  EBS resources.
- Jenkins has a Secret file credential with ID `aws-mysql-env`, containing
  `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and
  `MYSQL_ROOT_PASSWORD` as dotenv-style `KEY=value` lines. Use a non-root
  `MYSQL_USER`; never put this file in Git or pipeline logs.
- Do not use AWS root credentials.
- Ensure the EKS API endpoint access policy admits Jenkins. If using a
  restricted public endpoint, allow Jenkins' stable egress address.

## Run from Jenkins

Run the Jenkins pipeline with `AWS_CLUSTER_ADMIN_CIDR` set to the current
approved public IPv4 CIDR. The defaults create `student-management` and deploy
to it. The ordered stages check out code, clean/build/test, analyze with
SonarQube, publish to Nexus, build and push the Docker Hub image, verify AWS
credentials, provision EKS using Terraform, update kubeconfig, then deploy
MySQL and the application. AWS shared credentials are supplied through
`AWS_SHARED_CREDENTIALS_FILE`; credential contents are not printed or copied
into environment variables.

The AWS identity must be authorized to create VPC, IAM, EKS, and EBS CSI
resources, and to access the resulting EKS cluster for Kubernetes deployment.
Do not broaden the Kubernetes API allowlist to `0.0.0.0/0`.

Terraform state and the `.terraform` working directory are local to the
Jenkins workspace; restrict workspace access, retain the workspace between
runs, and do not archive or commit state files. Losing that state after apply
can leave AWS resources unmanaged.

## Current scope

The deployment uses a single MySQL StatefulSet with a 4 GiB gp3 EBS volume
and one Spring Boot replica. The app is exposed as NodePort `30089`; ensure
the node security group allows this port from the intended client addresses.
Use `kubectl get nodes -o wide` to find a worker's public address; the app is
available at `http://<node-public-ip>:30089/student/swagger-ui/index.html`.

The Terraform EKS configuration creates a separate `student-management`
cluster. It does not import, adopt, or modify the manually created
`amazing-hiphop-rainbow` cluster.
