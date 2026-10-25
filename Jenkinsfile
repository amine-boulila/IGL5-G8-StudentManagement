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

    }

    }
}