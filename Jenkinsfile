pipeline {
    agent any

    parameters {
        string(
            name: 'AWS_CLUSTER_ADMIN_CIDR',
            defaultValue: '',
            description: 'Public IPv4 CIDR allowed to reach the EKS API, for example your fixed public IP with /32. Leave blank to skip the AWS plan.'
        )
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {
        stage('Build and test') {
            steps {
                checkout scm
                sh 'bash ./mvnw --batch-mode clean verify'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('SonarQube analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    sh 'bash ./mvnw --batch-mode sonar:sonar -Dsonar.host.url=http://host.docker.internal:9000'
                }
            }
        }

        stage('Publish to Nexus') {
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

        stage('Plan AWS infrastructure (no apply)') {
            when {
                expression {
                    return params.AWS_CLUSTER_ADMIN_CIDR != null &&
                        !params.AWS_CLUSTER_ADMIN_CIDR.trim().isEmpty()
                }
            }
            steps {
                withCredentials([file(
                    credentialsId: 'awscredentials',
                    variable: 'AWS_SHARED_CREDENTIALS_FILE'
                )]) {
                    withEnv([
                        'PATH+TERRAFORM=/var/jenkins_home/tools/terraform/1.17.0',
                        'AWS_PROFILE=default',
                        'AWS_DEFAULT_REGION=us-east-1',
                        "TF_VAR_cluster_admin_cidr=${params.AWS_CLUSTER_ADMIN_CIDR}"
                    ]) {
                        sh '''
                            set -eu
                            aws sts get-caller-identity --query Arn --output text >/dev/null
                            terraform -chdir=terraform/aws init -input=false
                            terraform -chdir=terraform/aws fmt -check -recursive
                            terraform -chdir=terraform/aws validate
                            terraform -chdir=terraform/aws plan -input=false -no-color
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
