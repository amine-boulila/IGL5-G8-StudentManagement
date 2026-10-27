pipeline {
    agent any

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
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/student-management-*.jar,target/site/jacoco/**', fingerprint: true
        }
    }
}
