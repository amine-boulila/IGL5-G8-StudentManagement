locals {
  namespace = "student-management-terraform"
  labels = {
    app_managed_by = "terraform"
  }
}

resource "kubernetes_namespace_v1" "student_management" {
  metadata {
    name   = local.namespace
    labels = local.labels
  }
}

resource "kubernetes_secret_v1" "mysql_credentials" {
  metadata {
    name      = "mysql-credentials"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
  }

  data = {
    MYSQL_DATABASE      = var.mysql_database
    MYSQL_USER          = var.mysql_user
    MYSQL_PASSWORD      = var.mysql_password
    MYSQL_ROOT_PASSWORD = var.mysql_root_password
  }

  type = "Opaque"
}

resource "kubernetes_config_map_v1" "application" {
  metadata {
    name      = "student-management-config"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
  }

  data = {
    SPRING_JPA_SHOW_SQL = "false"
  }
}

resource "kubernetes_service_v1" "mysql" {
  metadata {
    name      = "mysql"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
  }

  spec {
    cluster_ip = "None"
    selector = {
      app = "mysql"
    }

    port {
      name        = "mysql"
      port        = 3306
      target_port = "mysql"
    }
  }
}

resource "kubernetes_stateful_set_v1" "mysql" {
  metadata {
    name      = "mysql"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
  }

  spec {
    service_name = kubernetes_service_v1.mysql.metadata[0].name
    replicas     = 1

    selector {
      match_labels = {
        app = "mysql"
      }
    }

    template {
      metadata {
        labels = {
          app = "mysql"
        }
      }

      spec {
        container {
          name  = "mysql"
          image = "mysql:8.4"

          port {
            name           = "mysql"
            container_port = 3306
          }

          dynamic "env" {
            for_each = {
              MYSQL_DATABASE      = "MYSQL_DATABASE"
              MYSQL_USER          = "MYSQL_USER"
              MYSQL_PASSWORD      = "MYSQL_PASSWORD"
              MYSQL_ROOT_PASSWORD = "MYSQL_ROOT_PASSWORD"
            }

            content {
              name = env.key

              value_from {
                secret_key_ref {
                  name = kubernetes_secret_v1.mysql_credentials.metadata[0].name
                  key  = env.value
                }
              }
            }
          }

          startup_probe {
            exec {
              command = [
                "sh",
                "-c",
                "mysqladmin ping -h 127.0.0.1 -u root -p\"$MYSQL_ROOT_PASSWORD\" --silent"
              ]
            }

            period_seconds    = 10
            failure_threshold = 30
          }

          readiness_probe {
            exec {
              command = [
                "sh",
                "-c",
                "mysqladmin ping -h 127.0.0.1 -u root -p\"$MYSQL_ROOT_PASSWORD\" --silent"
              ]
            }

            period_seconds    = 5
            timeout_seconds   = 3
            failure_threshold = 3
          }

          volume_mount {
            name       = "mysql-data"
            mount_path = "/var/lib/mysql"
          }
        }
      }
    }

    volume_claim_template {
      metadata {
        name = "mysql-data"
      }

      spec {
        access_modes = ["ReadWriteOnce"]

        resources {
          requests = {
            storage = "2Gi"
          }
        }
      }
    }
  }

  wait_for_rollout = true

  depends_on = [
    kubernetes_secret_v1.mysql_credentials
  ]
}

resource "kubernetes_deployment_v1" "application" {
  metadata {
    name      = "student-management"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
    labels    = local.labels
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "student-management"
      }
    }

    template {
      metadata {
        labels = {
          app = "student-management"
        }
      }

      spec {
        container {
          name              = "app"
          image             = var.app_image
          image_pull_policy = "Never"

          port {
            name           = "http"
            container_port = 8089
          }

          env_from {
            config_map_ref {
              name = kubernetes_config_map_v1.application.metadata[0].name
            }
          }

          env {
            name = "SPRING_DATASOURCE_URL"
            value = format(
              "jdbc:mysql://mysql:3306/%s?createDatabaseIfNotExist=true",
              var.mysql_database
            )
          }

          env {
            name = "SPRING_DATASOURCE_USERNAME"

            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.mysql_credentials.metadata[0].name
                key  = "MYSQL_USER"
              }
            }
          }

          env {
            name = "SPRING_DATASOURCE_PASSWORD"

            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.mysql_credentials.metadata[0].name
                key  = "MYSQL_PASSWORD"
              }
            }
          }

          startup_probe {
            tcp_socket {
              port = "http"
            }

            period_seconds    = 10
            failure_threshold = 30
          }

          readiness_probe {
            tcp_socket {
              port = "http"
            }

            period_seconds    = 5
            timeout_seconds   = 3
            failure_threshold = 3
          }

          liveness_probe {
            tcp_socket {
              port = "http"
            }

            period_seconds    = 10
            timeout_seconds   = 3
            failure_threshold = 3
          }
        }
      }
    }
  }

  wait_for_rollout = true

  depends_on = [
    kubernetes_stateful_set_v1.mysql
  ]
}

resource "kubernetes_service_v1" "application" {
  metadata {
    name      = "student-management"
    namespace = kubernetes_namespace_v1.student_management.metadata[0].name
  }

  spec {
    type = "NodePort"

    selector = {
      app = "student-management"
    }

    port {
      name        = "http"
      port        = 8089
      target_port = "http"
      node_port   = 30090
    }
  }
}
