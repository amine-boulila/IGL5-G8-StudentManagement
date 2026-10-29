locals {
  name = var.cluster_name
  tags = {
    Project   = "student-management"
    ManagedBy = "Terraform"
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.21.0"

  name = local.name
  cidr = "10.42.0.0/16"

  azs            = slice(data.aws_availability_zones.available.names, 0, 3)
  public_subnets = ["10.42.0.0/20", "10.42.16.0/20", "10.42.32.0/20"]

  create_igw              = true
  enable_nat_gateway      = false
  enable_dns_hostnames    = true
  enable_dns_support      = true
  map_public_ip_on_launch = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"
  }

  tags = local.tags
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "20.37.1"

  cluster_name    = local.name
  cluster_version = "1.35"

  cluster_endpoint_private_access          = true
  cluster_endpoint_public_access           = true
  cluster_endpoint_public_access_cidrs     = [var.cluster_admin_cidr]
  enable_cluster_creator_admin_permissions = true

  authentication_mode = "API"

  cluster_addons = {
    coredns            = { most_recent = true }
    kube-proxy         = { most_recent = true }
    vpc-cni            = { most_recent = true }
    aws-ebs-csi-driver = { most_recent = true }
  }

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.public_subnets

  node_security_group_additional_rules = {
    application_node_port = {
      description = "Allow the application NodePort from the administrator CIDR"
      protocol    = "tcp"
      from_port   = 30089
      to_port     = 30089
      type        = "ingress"
      cidr_blocks = [var.cluster_admin_cidr]
    }
  }

  eks_managed_node_groups = {
    student_management = {
      name                     = "${local.name}-workers"
      use_name_prefix          = false
      iam_role_name            = "${local.name}-workers"
      iam_role_use_name_prefix = false
      subnet_ids               = module.vpc.public_subnets
      instance_types           = ["t3.medium"]
      ami_type                 = "AL2023_x86_64_STANDARD"
      min_size                 = 1
      max_size                 = 2
      desired_size             = 1
      disk_size                = 30
      iam_role_additional_policies = {
        AmazonEBSCSIDriverPolicy = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
      }
    }
  }

  tags = local.tags
}
