def withAwsCreds(Closure body) {
    withCredentials([file(credentialsId: 'aws-lab-creds', variable: 'AWS_CREDS_FILE')]) {
        withEnv(["AWS_SHARED_CREDENTIALS_FILE=${env.AWS_CREDS_FILE}"]) {
            body()
        }
    }
}

pipeline {
    agent any

    options {
        timestamps()
    }

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    environment {
        SONAR_URL = 'http://host.docker.internal:9000'
        NEXUS_URL = 'http://host.docker.internal:8081'
        SONAR_PROJECT_KEY = 'IGL5-G8-StudentManagement'
        AWS_DEFAULT_REGION = 'us-east-1'
        EKS_CLUSTER_NAME = 'student-management-eks'
        EKS_ROLE_NAME = 'LabRole'
    }

    stages {

        stage('Checkout Git') {
            steps {
                git branch: 'feature/amine',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/amine-boulila/IGL5-G8-StudentManagement.git'
            }
        }

        stage('MVN Clean') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('Artifact Construction') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('MVN SonarQube') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonarqube-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988:sonar \
                          -Dsonar.host.url="$SONAR_URL" \
                          -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
                          -Dsonar.token="$SONAR_TOKEN"
                    '''
                }
            }
        }

        stage('Publish to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    script {
                        def xmlEscape = { value ->
                            value.replace('&', '&amp;')
                                 .replace('<', '&lt;')
                                 .replace('>', '&gt;')
                                 .replace('"', '&quot;')
                                 .replace("'", '&apos;')
                        }

                        writeFile file: 'jenkins-settings.xml',
                            text: """<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <servers>
    <server>
      <id>nexus-snapshots</id>
      <username>${xmlEscape(env.NEXUS_USER)}</username>
      <password>${xmlEscape(env.NEXUS_PASSWORD)}</password>
    </server>
    <server>
      <id>nexus-releases</id>
      <username>${xmlEscape(env.NEXUS_USER)}</username>
      <password>${xmlEscape(env.NEXUS_PASSWORD)}</password>
    </server>
  </servers>
</settings>"""

                        try {
                            sh '''
                                set +x
                                mvn -s jenkins-settings.xml deploy \
                                  -DskipTests \
                                  -DaltSnapshotDeploymentRepository=nexus-snapshots::http://host.docker.internal:8081/repository/maven-snapshots/ \
                                  -DaltReleaseDeploymentRepository=nexus-releases::http://host.docker.internal:8081/repository/maven-releases/
                            '''
                        } finally {
                            sh 'rm -f jenkins-settings.xml'
                        }
                    }
                }
            }
        }

        stage('Building Our Image') {
            steps {
                sh '''
                    docker build \
                      -t student-management:${BUILD_NUMBER} \
                      .
                '''
            }
        }

        stage('Deploy Our Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x

                        echo "$DOCKERHUB_TOKEN" |
                            docker login \
                                --username "$DOCKERHUB_USER" \
                                --password-stdin

                        docker tag \
                            "student-management:${BUILD_NUMBER}" \
                            "$DOCKERHUB_USER/student-management:${BUILD_NUMBER}"

                        docker tag \
                            "student-management:${BUILD_NUMBER}" \
                            "$DOCKERHUB_USER/student-management:latest"

                        docker push \
                            "$DOCKERHUB_USER/student-management:${BUILD_NUMBER}"

                        docker push \
                            "$DOCKERHUB_USER/student-management:latest"

                        docker logout
                    '''
                }
            }
        }

        stage('Test AWS Credentials') {
            steps {
                withAwsCreds {
                    sh '''
                        set +x
                        aws sts get-caller-identity
                    '''
                }
            }
        }

        stage('Terraform Init and Verify S3 State') {
            steps {
                withAwsCreds {
                    dir('terraform') {
                        sh '''
                            set +x

                            echo "Initializing Terraform with the configured S3 backend..."
                            terraform init -reconfigure -input=false

                            echo "Validating Terraform configuration..."
                            terraform validate

                            echo "Checking existing resources in Terraform state..."
                            terraform state list > state-resources.txt

                            if [ ! -s state-resources.txt ]; then
                                echo "Terraform state is empty. A new infrastructure deployment will be planned."
                            else
                                echo "Existing state detected:"
                                cat state-resources.txt
                            fi

                        '''
                    }
                }
            }
        }

        stage('Terraform Plan') {
            steps {
                withAwsCreds {
                    dir('terraform') {
                        sh '''
                            set +x

                            echo "Creating Terraform plan..."
                            AWS_ACCOUNT_ID=$(aws sts get-caller-identity \
                                --query Account \
                                --output text)
                            EKS_ROLE_ARN="arn:aws:iam::${AWS_ACCOUNT_ID}:role/${EKS_ROLE_NAME}"

                            terraform plan \
                                -input=false \
                                -var="cluster_role_arn=$EKS_ROLE_ARN" \
                                -var="node_role_arn=$EKS_ROLE_ARN" \
                                -out=tfplan

                            echo "Displaying planned infrastructure changes..."
                            terraform show -no-color tfplan
                        '''
                    }
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                withAwsCreds {
                    dir('terraform') {
                        sh '''
                            set +x

                            echo "Applying the saved Terraform plan..."
                            terraform apply \
                                -input=false \
                                tfplan
                        '''
                    }
                }
            }
        }

        stage('Update Kubeconfig') {
            steps {
                withAwsCreds {
                    sh '''
                        set +x

                        echo "Updating kubeconfig for EKS..."
                        aws eks update-kubeconfig \
                            --region "$AWS_DEFAULT_REGION" \
                            --name "$EKS_CLUSTER_NAME"

                        echo "Checking Kubernetes connection..."
                        kubectl get nodes
                    '''
                }
            }
        }

        stage('Ensure EBS CSI Add-on') {
            steps {
                withAwsCreds {
                    sh '''
                        set +x

                        if aws eks describe-addon \
                            --cluster-name "$EKS_CLUSTER_NAME" \
                            --addon-name aws-ebs-csi-driver \
                            --region "$AWS_DEFAULT_REGION" >/dev/null 2>&1; then
                            echo "AWS EBS CSI add-on is already installed."
                        else
                            echo "Installing AWS EBS CSI add-on..."
                            aws eks create-addon \
                                --cluster-name "$EKS_CLUSTER_NAME" \
                                --addon-name aws-ebs-csi-driver \
                                --region "$AWS_DEFAULT_REGION"
                        fi

                        aws eks wait addon-active \
                            --cluster-name "$EKS_CLUSTER_NAME" \
                            --addon-name aws-ebs-csi-driver \
                            --region "$AWS_DEFAULT_REGION"
                    '''
                }
            }
        }

        stage('Deploy to AWS Kubernetes') {
            steps {
                withAwsCreds {
                    sh '''
                        set +x

                        echo "Deploying MySQL first..."
                        kubectl apply -f kubernetes/mysql.yaml
                        kubectl rollout status deployment/mysql --timeout=300s

                        echo "Deploying the application and its service..."
                        kubectl apply -f kubernetes/deployment.yaml
                        kubectl apply -f kubernetes/service.yaml
                        kubectl rollout status deployment/student-management --timeout=300s

                        echo "Checking deployments..."
                        kubectl get deployments
                        echo "Checking pods..."
                        kubectl get pods
                        echo "Checking services..."
                        kubectl get services
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'rm -f jenkins-settings.xml || true'
        }

        success {
            echo 'CI/CD pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the first failed stage in Console Output.'
        }
    }
}