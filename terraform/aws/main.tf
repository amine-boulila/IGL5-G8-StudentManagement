locals {
  tags = {
    Project   = "student-management"
    ManagedBy = "Terraform"
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_vpc" "this" {
  cidr_block           = "10.42.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.tags, { Name = var.cluster_name })
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = merge(local.tags, { Name = "${var.cluster_name}-igw" })
}

resource "aws_subnet" "public" {
  count = length(var.public_subnet_cidrs)

  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = merge(local.tags, {
    Name                     = "${var.cluster_name}-public-${count.index + 1}"
    "kubernetes.io/role/elb" = "1"
  })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }

  tags = merge(local.tags, { Name = "${var.cluster_name}-public" })
}

resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_eks_cluster" "this" {
  name     = var.cluster_name
  role_arn = var.cluster_role_arn
  version  = var.cluster_version

  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = false
  }

  vpc_config {
    subnet_ids              = aws_subnet.public[*].id
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = [var.cluster_admin_cidr]
  }

  tags = local.tags
}

resource "aws_eks_access_entry" "cluster_admin" {
  cluster_name  = aws_eks_cluster.this.name
  principal_arn = var.cluster_admin_role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "cluster_admin" {
  cluster_name  = aws_eks_cluster.this.name
  principal_arn = aws_eks_access_entry.cluster_admin.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }
}

resource "aws_eks_node_group" "this" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.cluster_name}-workers"
  node_role_arn   = var.node_role_arn
  subnet_ids      = aws_subnet.public[*].id
  instance_types  = ["t3.medium"]
  ami_type        = "AL2023_x86_64_STANDARD"
  disk_size       = 30

  scaling_config {
    min_size     = 1
    max_size     = 2
    desired_size = 1
  }

  tags = local.tags
}

resource "aws_eks_addon" "this" {
  for_each = toset(["coredns", "kube-proxy", "vpc-cni"])

  cluster_name                = aws_eks_cluster.this.name
  addon_name                  = each.key
  resolve_conflicts_on_create = "OVERWRITE"

  depends_on = [aws_eks_node_group.this]
}

resource "aws_security_group_rule" "application_node_port" {
  type              = "ingress"
  description       = "Allow the application and Grafana NodePorts from the administrator CIDR"
  protocol          = "tcp"
  from_port         = 30089
  to_port           = 30090
  cidr_blocks       = [var.cluster_admin_cidr]
  security_group_id = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}
