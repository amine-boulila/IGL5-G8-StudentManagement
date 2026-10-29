def withAwsLab(Closure body) {
    withCredentials([string(credentialsId: 'aws-lab-creds', variable: 'AWS_LAB_CREDS')]) {
        def credFile = "${env.WORKSPACE}/.aws-credentials"
        sh '''
            printf '%s' "$AWS_LAB_CREDS" \
              | sed -E 's/\\[default\\]//; s/(aws_(access_key_id|secret_access_key|session_token))/\\n\\1/g' \
              | sed -E 's/[[:space:]]+$//; /^$/d' > "$WORKSPACE/.aws-credentials.tmp"
            { echo "[default]"; cat "$WORKSPACE/.aws-credentials.tmp"; } > "$WORKSPACE/.aws-credentials"
            rm -f "$WORKSPACE/.aws-credentials.tmp"
        '''
        withEnv(["AWS_SHARED_CREDENTIALS_FILE=${credFile}",
                 "AWS_DEFAULT_REGION=us-east-1"]) {
            try {
                body()
            } finally {
                sh "rm -f '${credFile}'"
            }
        }
    }
}
pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }
    environment {
        DOCKER_REPO = 'louha9999/student-management'
        KUBECONFIG  = '/var/jenkins_home/.kube/config'

    }
    stages {
        stage('1 - Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('2 - Maven Clean') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('3 - Build Artifact') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('4 - Unit Tests') {
            steps {
                sh 'mvn test jacoco:report'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('5 - SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.projectKey=student-management
                    '''
                }
            }
        }
        stage('6 - Publish to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials',
                                                usernameVariable: 'NEXUS_USER',
                                                passwordVariable: 'NEXUS_PASS')]) {
                    sh '''
                        set -e
                        printf '<settings><servers><server><id>nexus</id><username>%s</username><password>%s</password></server></servers></settings>' "$NEXUS_USER" "$NEXUS_PASS" > settings.xml

                        mvn -B -e deploy -DskipTests -s settings.xml \
                        -DaltSnapshotDeploymentRepository=nexus::default::http://nexus:8081/repository/maven-snapshots/ \
                        -DaltReleaseDeploymentRepository=nexus::default::http://nexus:8081/repository/maven-releases/

                        rm -f settings.xml
                    '''
                }
            }
        }

        stage('7 - Building our image') {
            steps {
                sh 'docker build -t $DOCKER_REPO:${BUILD_NUMBER} -t $DOCKER_REPO:latest .'
            }
        }
        stage('8 -Deploy our image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_cred',
                                                usernameVariable: 'DOCKER_USER',
                                                passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKER_REPO:${BUILD_NUMBER}
                        docker push $DOCKER_REPO:latest
                        docker logout
                    '''
                }
            }
        }
        stage('9 - Test AWS Credentials') {
            steps {
                script {
                    withAwsLab {
                        sh "sed 's/=.*/=***/' $AWS_SHARED_CREDENTIALS_FILE"
                        sh 'aws sts get-caller-identity'
                    }
                }
            }
        }
        stage('10 - Setup Terraform') {
            steps {
                script {
                    withAwsLab {
                        dir('terraform') {
                            sh '''
                                terraform init -input=false
                                terraform validate
                                terraform apply -auto-approve -input=false
                            '''
                        }
                    }
                }
            }
        }
        stage('11 - Update Kubeconfig') {
            steps {
                script {
                    withAwsLab {
                        sh '''
                            mkdir -p "$(dirname $KUBECONFIG)"
                            aws eks update-kubeconfig --name devops-eks --region us-east-1
                            kubectl get nodes
                        '''
                    }
                }
            }
        }
        stage('12 - Deploy to AWS Kubernetes') {
            steps {
                script {
                    withAwsLab {
                        sh '''
                            kubectl apply -f k8s/mysql.yaml
                            kubectl rollout status deployment/mysql --timeout=180s
                            sed "s|IMAGE_PLACEHOLDER|$DOCKER_REPO:${BUILD_NUMBER}|" k8s/deployment.yaml | kubectl apply -f -
                            kubectl rollout status deployment/student-management --timeout=240s
                            kubectl get pods
                            kubectl get svc student-management
                        '''
                    }
                }
            }
        }
        stage('13 - Deploy Monitoring (Prometheus + Grafana)') {
            steps {
                withCredentials([string(credentialsId: 'grafana-admin-password', variable: 'GRAFANA_PASS')]) {
                    script {
                        withAwsLab {
                            sh '''
                                helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                                helm repo update

                                helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
                                --namespace monitoring --create-namespace \
                                --set grafana.adminPassword="$GRAFANA_PASS" \
                                --set grafana.service.type=LoadBalancer \
                                --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
                                --set prometheus.prometheusSpec.retention=3d \
                                --wait --timeout 10m

                                kubectl apply -f k8s/servicemonitor.yaml
                                kubectl get pods -n monitoring
                                kubectl get svc -n monitoring monitoring-grafana
                            '''
                        }
                    }
                }
            }
        }
    }
    post {
        unsuccessful {
            script {
                withAwsLab {
                    sh '''
                        aws eks update-kubeconfig --name devops-eks --region us-east-1 || true
                        echo "===== PODS (all namespaces) ====="
                        kubectl get pods -A -o wide || true
                        echo "===== EVENTS (recent) ====="
                        kubectl get events -A --sort-by=.lastTimestamp | tail -40 || true
                        echo "===== APP DESCRIBE ====="
                        kubectl describe pods -l app=student-management -n default || true
                        echo "===== APP LOGS ====="
                        kubectl logs -l app=student-management -n default --all-containers --tail=100 || true
                        echo "===== MYSQL LOGS ====="
                        kubectl logs deploy/mysql -n default --tail=100 || true
                        echo "===== MONITORING PODS ====="
                        kubectl get pods -n monitoring || true
                        kubectl describe pods -n monitoring | grep -A8 -i "events:" | tail -60 || true
                    '''
                }
            }
        }
        success {
            mail to: 'mlouhichi550@gmail.com',
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "The pipeline passed.\n\nBuild: ${env.BUILD_URL}"
        }
    }
}