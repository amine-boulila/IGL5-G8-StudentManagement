# Image Jenkins avec tous les outils de la pipeline :
# JDK 17 (inclus), Maven, Docker CLI, Terraform, kubectl, AWS CLI
FROM jenkins/jenkins:lts-jdk17

USER root

ARG DOCKER_VERSION=28.0.4
ARG TERRAFORM_VERSION=1.9.8
ARG KUBECTL_VERSION=v1.32.2

RUN apt-get update \
 && apt-get install -y --no-install-recommends maven curl unzip ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Docker CLI (le moteur Docker reste celui de Docker Desktop, via le socket monte)
RUN curl -fsSL "https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}.tgz" -o /tmp/docker.tgz \
 && tar -xzf /tmp/docker.tgz -C /tmp \
 && mv /tmp/docker/docker /usr/local/bin/docker \
 && rm -rf /tmp/docker /tmp/docker.tgz

# Terraform
RUN curl -fsSL "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip" -o /tmp/terraform.zip \
 && unzip -q /tmp/terraform.zip -d /usr/local/bin \
 && rm /tmp/terraform.zip

# kubectl
RUN curl -fsSL "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl" -o /usr/local/bin/kubectl \
 && chmod +x /usr/local/bin/kubectl

# AWS CLI v2
RUN curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip \
 && unzip -q /tmp/awscliv2.zip -d /tmp \
 && /tmp/aws/install \
 && rm -rf /tmp/aws /tmp/awscliv2.zip
