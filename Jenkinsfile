pipeline {
    agent any

    parameters {
        string(
            name: 'DOCKERHUB_IMAGE',
            defaultValue: '',
            description: 'Docker Hub image, for example yourusername/student-management'
        )
        booleanParam(
            name: 'APPLY_INFRASTRUCTURE',
            defaultValue: false,
            description: 'Run Terraform apply for the EKS infrastructure'
        )
    }

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    stages {
        stage('1 - Git Checkout') {
            steps {
                git(
                    branch: 'feature/jenkins-pipeline',
                    credentialsId: 'github-private-repo',
                    url: 'https://github.com/amine-boulila/IGL5-G8-StudentManagement.git'
                )
            }
        }

        stage('2 - Maven Clean') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean'
            }
        }

        stage('3 - Build Artifact') {
            steps {
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('4 - Unit Tests') {
            steps {
                sh './mvnw test jacoco:report'
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
                        ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.host.url=http://host.docker.internal:9000 \
                        -Dsonar.projectKey=student-management
                    '''
                }
            }
        }
        stage('Nexus Publish') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -eu
                        umask 077
                        settings_file="$PWD/.nexus-settings.xml"
                        trap 'rm -f "$settings_file"' EXIT

                        printf '%s\n' \
                          '<settings>' \
                          '  <servers>' \
                          '    <server>' \
                          '      <id>nexus</id>' \
                          "      <username>${NEXUS_USERNAME}</username>" \
                          "      <password>${NEXUS_PASSWORD}</password>" \
                          '    </server>' \
                          '  </servers>' \
                          '</settings>' > "$settings_file"

                        ./mvnw -s "$settings_file" deploy:deploy-file \
                          -Dfile=target/student-management-0.0.1-SNAPSHOT.jar \
                          -DpomFile=pom.xml \
                          -DrepositoryId=nexus \
                          -Durl=http://host.docker.internal:8081/repository/maven-snapshots/ \
                          -DgroupId=tn.esprit \
                          -DartifactId=student-management \
                          -Dversion=0.0.1-SNAPSHOT \
                          -Dpackaging=jar \
                          -DgeneratePom=false
                    '''
                }
            }
        }
        stage('Docker Build') {
            steps {
                sh '''
                    set -eu
                    docker build \
                      --tag student-management:${BUILD_NUMBER} \
                      .
                '''
            }
        }
        stage('Docker Hub Publish') {
            when {
                expression {
                    params.DOCKERHUB_IMAGE?.trim()
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    retry(3) {
                        sh '''
                            set -eu
                            image="${DOCKERHUB_IMAGE}:${BUILD_NUMBER}"
                            latest_image="${DOCKERHUB_IMAGE}:latest"
                            printf '%s' "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
                            trap 'docker logout >/dev/null 2>&1 || true' EXIT
                            docker tag "student-management:${BUILD_NUMBER}" "$image"
                            docker tag "student-management:${BUILD_NUMBER}" "$latest_image"
                            docker push "$image"
                            docker push "$latest_image"
                        '''
                    }
                }
            }
        }
        stage('Test AWS Credentials') {
            when {
                expression {
                    params.APPLY_INFRASTRUCTURE || params.DOCKERHUB_IMAGE?.trim()
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    ),
                    string(
                        credentialsId: 'aws-session-token',
                        variable: 'AWS_SESSION_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu
                        export AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}"
                        aws sts get-caller-identity --region "$AWS_DEFAULT_REGION"
                    '''
                }
            }
        }
        stage('Terraform Apply') {
            when {
                expression {
                    params.APPLY_INFRASTRUCTURE
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    ),
                    string(
                        credentialsId: 'aws-session-token',
                        variable: 'AWS_SESSION_TOKEN'
                    )
                ]) {
                    dir('terraform/eks-cluster') {
                        sh '''
                            set -eu
                            export AWS_DEFAULT_REGION=us-east-1
                            terraform init -input=false
                            terraform validate
                            terraform plan -input=false \
                              -var="aws_region=us-east-1" \
                              -var="aws_profile=" \
                              -var="enable_ebs_csi=false"
                            terraform apply -auto-approve \
                              -var="aws_region=us-east-1" \
                              -var="aws_profile=" \
                              -var="enable_ebs_csi=false"
                        '''
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            when {
                expression {
                    params.DOCKERHUB_IMAGE?.trim()
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    ),
                    string(
                        credentialsId: 'aws-session-token',
                        variable: 'AWS_SESSION_TOKEN'
                    ),
                    string(credentialsId: 'student-management-db-password', variable: 'DB_PASSWORD')
                ]) {
                    sh '''
                        set -eu
                        export AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}"

                        aws eks update-kubeconfig \
                          --region "$AWS_DEFAULT_REGION" \
                          --name student-management-eks

                        kubectl apply -f k8s/namespace.yaml
                        kubectl create secret generic student-management-db \
                          --namespace student-management \
                          --from-literal=DB_PASSWORD="$DB_PASSWORD" \
                          --dry-run=client -o yaml | kubectl apply -f -

                        kubectl apply -f k8s/mysql-deployment-ephemeral.yaml
                        kubectl apply -f k8s/mysql-service.yaml
                        kubectl apply -f k8s/app-deployment.yaml
                        kubectl apply -f k8s/app-service.yaml

                        kubectl set image deployment/student-management \
                          student-management="${DOCKERHUB_IMAGE}:${BUILD_NUMBER}" \
                          --namespace student-management
                        kubectl rollout status deployment/mysql \
                          --namespace student-management \
                          --timeout=5m
                        kubectl rollout status deployment/student-management \
                          --namespace student-management \
                          --timeout=5m
                        kubectl get service student-management \
                          --namespace student-management
                    '''
                }
            }
        }
        stage('Monitoring') {
            when {
                expression {
                    params.DOCKERHUB_IMAGE?.trim()
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    ),
                    string(
                        credentialsId: 'aws-session-token',
                        variable: 'AWS_SESSION_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu
                        export AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}"

                        kubectl apply -k k8s/monitoring
                        kubectl rollout status deployment/prometheus \
                          --namespace student-management \
                          --timeout=5m
                        kubectl rollout status deployment/grafana \
                          --namespace student-management \
                          --timeout=5m
                    '''
                }
            }
        }
    }
}
