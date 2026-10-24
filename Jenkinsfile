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
                sh 'chmod +x mvnw'
                sh './mvnw clean'
            }
        }

        stage('3 - Build Artifact') {
            steps {
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('4 - Unit Tests') {
            steps {
                sh './mvnw test jacoco:report'
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
                        ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.host.url=http://host.docker.internal:9000 \
                        -Dsonar.projectKey=student-management
                    '''
                }
            }
        }
        stage('Nexus Publish') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -eu
                        umask 077
                        settings_file="$PWD/.nexus-settings.xml"
                        trap 'rm -f "$settings_file"' EXIT

                        printf '%s\n' \
                          '<settings>' \
                          '  <servers>' \
                          '    <server>' \
                          '      <id>nexus</id>' \
                          "      <username>${NEXUS_USERNAME}</username>" \
                          "      <password>${NEXUS_PASSWORD}</password>" \
                          '    </server>' \
                          '  </servers>' \
                          '</settings>' > "$settings_file"

                        ./mvnw -s "$settings_file" deploy:deploy-file \
                          -Dfile=target/student-management-0.0.1-SNAPSHOT.jar \
                          -DpomFile=pom.xml \
                          -DrepositoryId=nexus \
                          -Durl=http://host.docker.internal:8081/repository/maven-snapshots/ \
                          -DgroupId=tn.esprit \
                          -DartifactId=student-management \
                          -Dversion=0.0.1-SNAPSHOT \
                          -Dpackaging=jar \
                          -DgeneratePom=false
                    '''
                }
            }
        }
    }
}