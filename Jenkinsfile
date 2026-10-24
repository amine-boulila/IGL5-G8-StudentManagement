
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
    }

    post {
        always {
            sh 'rm -f jenkins-settings.xml || true'
        }

        success {
            echo 'CI/CD pipeline completed successfully! Image pushed to Docker Hub.'
        }

        failure {
            echo 'Pipeline failed. Check the first failed stage in Console Output.'
        }
    }
}
