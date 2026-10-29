pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    // Start the pipeline when GitHub sends a push webhook to Jenkins (/github-webhook/)
    triggers {
        githubPush()
    }

    environment {
        NOTIFY_EMAIL = 'benjemaayassine00@gmail.com'
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
                                                  usernameVariable: 'NEXUS_USERNAME',
                                                  passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh 'mvn deploy -DskipTests -s .mvn/settings.xml'
                }
            }
        }

        stage('7 - Build Docker Image') {
            steps {
                sh 'docker build -t student-management:${BUILD_NUMBER} .'
            }
        }

        stage('8 - Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker tag student-management:${BUILD_NUMBER} $DOCKER_USER/student-management:${BUILD_NUMBER}
                        docker tag student-management:${BUILD_NUMBER} $DOCKER_USER/student-management:latest
                        docker push $DOCKER_USER/student-management:${BUILD_NUMBER}
                        docker push $DOCKER_USER/student-management:latest
                        docker logout
                    '''
                }
            }
        }

        stage('9 - Test AWS Credentials') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    sh 'aws sts get-caller-identity --region us-east-1'
                }
            }
        }

        stage('10 - Setup Terraform') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
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

        stage('11 - Update Kubeconfig') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    sh '''
                        aws eks update-kubeconfig --name student-management-eks --region us-east-1
                        kubectl get nodes
                    '''
                }
            }
        }

        stage('12 - Deploy to AWS Kubernetes') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE'),
                                 usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        kubectl apply -f k8s/mysql.yaml
                        kubectl rollout status deployment/mysql --timeout=180s
                        sed "s|IMAGE_PLACEHOLDER|$DOCKER_USER/student-management:${BUILD_NUMBER}|" k8s/deployment.yaml | kubectl apply -f -
                        kubectl apply -f k8s/service.yaml
                        kubectl rollout status deployment/student-management --timeout=300s
                        kubectl get pods
                        NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
                        echo "Application URL: http://$NODE_IP:30000/student/students/getAllStudents"
                    '''
                }
            }
        }

    }

    post {
        success {
            script {
                try {
                    emailext to: "${NOTIFY_EMAIL}",
                             subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                             body: "The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} succeeded. Details: ${env.BUILD_URL}"
                } catch (err) {
                    echo "Email notification failed: ${err.message}"
                }
            }
        }
        failure {
            script {
                try {
                    emailext to: "${NOTIFY_EMAIL}",
                             subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                             body: "The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} failed. Console output: ${env.BUILD_URL}console"
                } catch (err) {
                    echo "Email notification failed: ${err.message}"
                }
            }
        }
    }
}
