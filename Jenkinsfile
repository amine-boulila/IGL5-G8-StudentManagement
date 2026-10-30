pipeline {
    agent any

    environment {
        AWS_CREDENTIALS_ID = 'awscredentials'
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        AWS_MYSQL_ENV_CREDENTIALS_ID = 'aws-mysql-env'
        AWS_REGION = 'us-east-1'
        KUBECTL_VERSION = 'v1.35.0'
    }

    parameters {
        booleanParam(
            name: 'DEPLOY_TO_EKS',
            defaultValue: true,
            description: 'Deploy the built image and MySQL workload to the configured EKS cluster.'
        )
        string(
            name: 'EKS_CLUSTER_NAME',
            defaultValue: 'student-management',
            description: 'Name of the EKS cluster to deploy to.'
        )
        booleanParam(
            name: 'PROVISION_EKS_WITH_TERRAFORM',
            defaultValue: true,
            description: 'Create or update the separate student-management EKS infrastructure with Terraform. This can incur AWS charges.'
        )
        string(
            name: 'AWS_CLUSTER_ADMIN_CIDR',
            defaultValue: '',
            description: 'Public IPv4 CIDR allowed to reach the EKS API, for example your fixed public IP with /32. Leave blank to skip AWS provisioning.'
        )
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {
        stage('CHECKOUT GIT') {
            steps {
                checkout scm




            }
        }

        stage('MVN CLEAN') {
            steps {
                sh 'bash ./mvnw --batch-mode clean'

            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                sh 'bash ./mvnw --batch-mode package -DskipTests'





            }
        }

        stage('UNIT TESTS') {
            steps {
                sh 'bash ./mvnw --batch-mode verify'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('MVN SONARQUBE') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    sh 'bash ./mvnw --batch-mode sonar:sonar -Dsonar.host.url=http://host.docker.internal:9000'





                }
            }
        }

        stage('PUBLISH TO NEXUS') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-maven-deploy',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD'
                )]) {


                    sh '''
                        set +x
                        umask 077
                        settings_file="$(mktemp)"
                        trap 'rm -f "$settings_file"' EXIT

                        xml_escape() {
                            printf '%s' "$1" | sed -e 's/&/\\&amp;/g' -e 's/</\\&lt;/g' -e 's/>/\\&gt;/g' -e 's/"/\\&quot;/g' -e "s/'/\\&apos;/g"
                        }

                        nexus_username="$(xml_escape "$NEXUS_USERNAME")"
                        nexus_password="$(xml_escape "$NEXUS_PASSWORD")"
                        printf '%s\n' \
                            '<settings>' \
                            '  <servers>' \
                            '    <server>' \
                            '      <id>nexus-releases</id>' \
                            "      <username>${nexus_username}</username>" \
                            "      <password>${nexus_password}</password>" \
                            '    </server>' \
                            '    <server>' \
                            '      <id>nexus-snapshots</id>' \
                            "      <username>${nexus_username}</username>" \
                            "      <password>${nexus_password}</password>" \
                            '    </server>' \
                            '  </servers>' \
                            '</settings>' > "$settings_file"

                        bash ./mvnw --batch-mode --settings "$settings_file" deploy -DskipTests









                    '''
                }
            }
        }

        stage('BUILDING OUR IMAGE') {




            steps {
                sh '''
                    set -eu
                    if ! command -v docker >/dev/null 2>&1; then
                        echo "Docker CLI is missing from this Jenkins agent."
                        exit 1
                    fi
                    if ! docker info >/dev/null 2>&1; then
                        echo "Jenkins cannot reach a Docker daemon. Configure the approved Docker socket or builder."
                        exit 1
                    fi
                    docker build --pull --tag "student-management:${BUILD_NUMBER}" .
                '''
            }
        }

        stage('DEPLOY OUR IMAGE') {




            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.DOCKERHUB_CREDENTIALS_ID,
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_PASSWORD'
                )]) {
                    sh '''
                        set +x
                        set -eu
                        case "$DOCKERHUB_USERNAME" in
                            ""|*[!A-Za-z0-9._-]*)
                                echo "Docker Hub username contains unsupported characters."
                                exit 1
                                ;;
                        esac

                        export DOCKER_CONFIG="$(mktemp -d)"
                        trap 'docker logout >/dev/null 2>&1 || true; rm -rf "$DOCKER_CONFIG"' EXIT
                        printf '%s' "$DOCKERHUB_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
                        docker tag "student-management:${BUILD_NUMBER}" "$DOCKERHUB_USERNAME/student-management:${BUILD_NUMBER}"
                        docker push "$DOCKERHUB_USERNAME/student-management:${BUILD_NUMBER}"
                    '''
                }
            }
        }

        stage('Test AWS Credentials') {
            when {
                expression { params.DEPLOY_TO_EKS || params.PROVISION_EKS_WITH_TERRAFORM }


            }
            steps {
                withCredentials([file(
                    credentialsId: env.AWS_CREDENTIALS_ID,
                    variable: 'AWS_SHARED_CREDENTIALS_FILE'
                )]) {
                    withEnv([
                        'AWS_PROFILE=default',
                        "AWS_DEFAULT_REGION=${env.AWS_REGION}",
                        "EKS_CLUSTER_NAME=${params.EKS_CLUSTER_NAME}",
                        "DEPLOY_TO_EKS=${params.DEPLOY_TO_EKS}",
                        "PROVISION_EKS_WITH_TERRAFORM=${params.PROVISION_EKS_WITH_TERRAFORM}"
                    ]) {
                        sh '''
                            set -eu
                            aws sts get-caller-identity --query Arn --output text >/dev/null
                            if [ "$PROVISION_EKS_WITH_TERRAFORM" != "true" ] && [ "$DEPLOY_TO_EKS" = "true" ]; then
                                cluster_status="$(aws eks describe-cluster --name "$EKS_CLUSTER_NAME" --query 'cluster.status' --output text)"
                                if [ "$cluster_status" != "ACTIVE" ]; then
                                    echo "The configured EKS cluster is not ACTIVE."
                                    exit 1
                                fi
                            fi
                            echo "AWS credentials verified."
                        '''
                    }
                }
            }
        }

        stage('SETUP TERRAFORM') {
            when {
                expression { params.PROVISION_EKS_WITH_TERRAFORM }


            }
            steps {
                withCredentials([file(
                    credentialsId: env.AWS_CREDENTIALS_ID,
                    variable: 'AWS_SHARED_CREDENTIALS_FILE'
                )]) {
                    withEnv([
                        'PATH+TERRAFORM=/var/jenkins_home/tools/terraform/1.17.0',
                        'AWS_PROFILE=default',
                        "AWS_DEFAULT_REGION=${env.AWS_REGION}",
                        "TF_VAR_cluster_admin_cidr=${params.AWS_CLUSTER_ADMIN_CIDR}"
                    ]) {
                        dir('terraform/aws') {
                            sh '''
                                set -eu
                                if [ -z "$TF_VAR_cluster_admin_cidr" ]; then
                                    echo "Set AWS_CLUSTER_ADMIN_CIDR before enabling Terraform provisioning."
                                    exit 1
                                fi
                                aws sts get-caller-identity --query Arn --output text >/dev/null
                                terraform init -input=false
                                terraform fmt -check -recursive
                                terraform validate
                                rm -f student-management.tfplan
                                trap 'rm -f student-management.tfplan' EXIT
                                terraform plan -input=false -no-color -out=student-management.tfplan
                                terraform apply -input=false -auto-approve student-management.tfplan




                            '''
                        }
                    }
                }
            }
        }

        stage('UPDATE KUBECONFIG') {
            when {
                expression { params.DEPLOY_TO_EKS }


            }
            steps {
                withCredentials([file(
                    credentialsId: env.AWS_CREDENTIALS_ID,
                    variable: 'AWS_SHARED_CREDENTIALS_FILE'
                )]) {
                    withEnv([
                        'AWS_PROFILE=default',
                        "AWS_DEFAULT_REGION=${env.AWS_REGION}",
                        "EKS_CLUSTER_NAME=${params.EKS_CLUSTER_NAME}"
                    ]) {


                        sh '''
                            set -eu
                            tools_dir="$WORKSPACE/.tools/bin"
                            mkdir -p "$tools_dir" "$WORKSPACE/.kube"
                            kubectl="$tools_dir/kubectl"
                            kubectl_url="https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"
                            curl --fail --silent --show-error --location "$kubectl_url" -o "$kubectl"
                            curl --fail --silent --show-error --location "${kubectl_url}.sha256" -o "${kubectl}.sha256"
                            printf '%s  %s\\n' "$(cat "${kubectl}.sha256")" "$kubectl" | sha256sum --check --status
                            chmod 0755 "$kubectl"
                            rm -f "${kubectl}.sha256"

                            export KUBECONFIG="$WORKSPACE/.kube/config"
                            aws eks update-kubeconfig --name "$EKS_CLUSTER_NAME" --region "$AWS_DEFAULT_REGION" --kubeconfig "$KUBECONFIG" >/dev/null
                            "$kubectl" wait --for=condition=Ready nodes --all --timeout=10m




























                        '''
                    }
                }
            }
        }

        stage('DEPLOY TO AWS KUBERNETES') {
            when {
                expression { params.DEPLOY_TO_EKS }


            }
            steps {
                withCredentials([
                    file(credentialsId: env.AWS_CREDENTIALS_ID, variable: 'AWS_SHARED_CREDENTIALS_FILE'),
                    file(credentialsId: env.AWS_MYSQL_ENV_CREDENTIALS_ID, variable: 'MYSQL_ENV_FILE'),
                    usernamePassword(
                        credentialsId: env.DOCKERHUB_CREDENTIALS_ID,
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                    withEnv([
                        "PATH+KUBECTL=${env.WORKSPACE}/.tools/bin",
                        'AWS_PROFILE=default',
                        "AWS_DEFAULT_REGION=${env.AWS_REGION}"
                    ]) {
                        sh '''
                            set +x
                            set -eu
                            export KUBECONFIG="$WORKSPACE/.kube/config"

                            for key in MYSQL_DATABASE MYSQL_USER MYSQL_PASSWORD MYSQL_ROOT_PASSWORD; do
                                if ! grep -q "^${key}=" "$MYSQL_ENV_FILE"; then
                                    echo "The AWS MySQL environment credential is missing a required key."
                                    exit 1
                                fi
                            done

                            case "$DOCKERHUB_USERNAME" in
                                ""|*[!A-Za-z0-9._-]*)
                                    echo "Docker Hub username contains unsupported characters."
                                    exit 1
                                    ;;
                            esac

                            kubectl create namespace student-management --dry-run=client -o yaml | kubectl apply -f -
                            kubectl create secret generic mysql-credentials \
                                --namespace student-management \
                                --from-env-file="$MYSQL_ENV_FILE" \
                                --dry-run=client -o yaml | kubectl apply -f -
                            kubectl apply -f k8s/aws/mysql.yaml
                            kubectl rollout status statefulset/mysql --namespace student-management --timeout=10m

                            image="$DOCKERHUB_USERNAME/student-management:${BUILD_NUMBER}"
                            sed "s|__IMAGE__|${image}|g" k8s/aws/app.yaml | kubectl apply -f -
                            kubectl rollout status deployment/student-management --namespace student-management --timeout=10m
                            kubectl get service student-management --namespace student-management



                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/student-management-*.jar,target/site/jacoco/**', fingerprint: true
        }
    }
}