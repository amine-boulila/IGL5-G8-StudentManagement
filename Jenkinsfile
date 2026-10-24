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
                bat 'mvnw.cmd clean'
            }
        }

        stage('3 - Build Artifact') {
            steps {
                bat 'mvnw.cmd package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('4 - Unit Tests') {
            steps {
                bat 'mvnw.cmd test jacoco:report'
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
                    bat '''
                        mvnw.cmd sonar:sonar ^
                        -Dsonar.token=%SONAR_TOKEN% ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.projectKey=student-management
                    '''
                }
            }
        }

    }
}