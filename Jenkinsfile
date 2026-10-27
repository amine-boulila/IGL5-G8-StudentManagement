pipeline {
    agent any

    parameters {
        string(
            name: 'GITHUB_BRANCH',
            defaultValue: 'feature/ahmed-student-management',
            description: 'Git branch to build from the private GitHub repository'
        )
    }

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('Build and test') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.GITHUB_BRANCH}"]],
                    userRemoteConfigs: [[
                        credentialsId: 'github-repo-read',
                        url: 'https://github.com/amine-boulila/IGL5-G8-StudentManagement.git'
                    ]]
                ])
                sh 'bash mvnw --batch-mode clean verify'
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
                    sh 'bash mvnw --batch-mode sonar:sonar -Dsonar.host.url=http://host.docker.internal:9000'
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
