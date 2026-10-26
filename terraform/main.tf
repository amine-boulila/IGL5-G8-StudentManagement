# Cluster EKS sur AWS Learner Lab
# Contraintes du Learner Lab : pas de creation de role IAM -> on reutilise "LabRole"
#                              on utilise le VPC par defaut

terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Le chemin du fichier d'etat est fourni par Jenkins : terraform init -backend-config="path=..."
  backend "local" {}
}

provider "aws" {
  region = var.region
}

# Role IAM fourni par le Learner Lab (utilise par le cluster et par les noeuds)
data "aws_iam_role" "lab" {
  name = "LabRole"
}

# VPC par defaut et ses sous-reseaux (zones a, b, c : EKS ne supporte pas us-east-1e)
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "availability-zone"
    values = ["${var.region}a", "${var.region}b", "${var.region}c"]
  }
}

# Plan de controle Kubernetes (gere par AWS)
resource "aws_eks_cluster" "this" {
  name     = var.cluster_name
  role_arn = data.aws_iam_role.lab.arn

  vpc_config {
    subnet_ids = data.aws_subnets.default.ids
  }
}

# Noeuds de travail (instances EC2 qui executent les pods)
resource "aws_eks_node_group" "this" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.cluster_name}-nodes"
  node_role_arn   = data.aws_iam_role.lab.arn
  subnet_ids      = data.aws_subnets.default.ids
  instance_types  = [var.node_instance_type]

  scaling_config {
    desired_size = var.node_count
    min_size     = 1
    max_size     = var.node_count + 1
  }
}
