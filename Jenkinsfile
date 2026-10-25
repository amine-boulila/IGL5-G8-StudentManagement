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

    }
}