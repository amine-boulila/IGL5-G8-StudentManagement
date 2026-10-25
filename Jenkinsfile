pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
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
        stage('Publish to Nexus') {
        steps {
            withCredentials([usernamePassword(credentialsId: 'nexus-credentials',
                                            usernameVariable: 'NEXUS_USER',
                                            passwordVariable: 'NEXUS_PASS')]) {
                sh '''
                        printf '<settings><servers><server><id>nexus</id><username>%s</username><password>%s</password></server></servers></settings>' "$NEXUS_USER" "$NEXUS_PASS" > settings.xml
                        mvn deploy -DskipTests -s settings.xml
                        rm -f settings.xml
                    '''
            }
        }
    }

    }
}