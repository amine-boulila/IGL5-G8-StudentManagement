pipeline {
    agent any

    stages {
        stage('Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Checkout') {
            steps {
                sh 'pwd'
                sh 'ls -la'
                sh 'git log -1 --oneline'
                sh 'test -f pom.xml && echo "pom.xml found: checkout OK" || (echo "pom.xml NOT found" && exit 1)'
            }
        }
    }
}