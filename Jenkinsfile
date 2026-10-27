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

    }
}