# AWS infrastructure plan

The AWS configuration is separate from the existing local Docker Desktop
deployment in `terraform/`. It defines an ECR image repository and an EKS
cluster in `us-east-1`.

The EKS API is public but restricted to a caller-supplied IPv4 CIDR. Worker
nodes run in public subnets to avoid NAT Gateway charges; no SSH access is
configured. The managed node group starts with one `t3.medium` instance and
can scale to two. EKS control-plane, EC2, EBS, and image storage incur AWS
charges when provisioned. The pipeline only runs `terraform plan`; it never
applies or creates these resources.

## Jenkins prerequisites

- AWS CLI v2 and Terraform 1.17.0 are installed on the Jenkins executor.
- Jenkins has a Secret file credential with ID `awscredentials`. It must be
  an AWS shared-credentials INI file containing a `[default]` profile. Keep
  the file private and never print its contents in pipeline logs.
- The AWS identity needs permissions to inspect account/region details and to
  plan the ECR, VPC, IAM, and EKS resources. Do not use root credentials.
- Use a stable public IPv4 CIDR for authorized cluster administration. A
  fixed administrator IP is typically entered as `address/32`. If Jenkins
  must later run `kubectl` against the cluster, include its stable egress
  address as well.

## Review a plan

Run the Jenkins pipeline and set the `AWS_CLUSTER_ADMIN_CIDR` build parameter
to the allowed CIDR. Leave it blank to skip the AWS plan. The stage initializes
Terraform, validates the configuration, and displays the proposed changes.
It does not run `terraform apply`.

Do not broaden the Kubernetes API allowlist to `0.0.0.0/0`. Review the plan
and estimated AWS costs before any separately authorized apply. Terraform
state and the `.terraform` working directory are local to the Jenkins
workspace; restrict workspace access and do not archive or commit state files.

## Current scope

This first AWS plan creates the networking, EKS control plane and managed
worker group, plus the ECR repository and image lifecycle policy. It does not
deploy the Spring application or MySQL. Those workloads and the pipeline's
image build/push will be a later step after the infrastructure plan has been
reviewed.
