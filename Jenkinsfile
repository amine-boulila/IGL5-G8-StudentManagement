pipeline {
    agent any

    options {
        timestamps()
        skipDefaultCheckout()   // evite la colonne "Declarative: Checkout SCM" en double
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME   = 'student-management'
        AWS_REGION   = 'us-east-1'
        AWS_DEFAULT_REGION = 'us-east-1'
        CLUSTER_NAME = 'student-management-eks'
        TF_IN_AUTOMATION = 'true'
    }

    stages {

        stage('CHECKOUT GIT') {
            steps {
                // Recupere le code de la branche configuree dans le job
                checkout scm
            }
        }

        stage('MVN CLEAN') {
            steps {
                sh 'mvn -B clean'
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                // Construit le .jar sans lancer les tests (ils ont leur propre etape)
                sh 'mvn -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('UNIT TESTS') {
            steps {
                // Tests JUnit (avec et sans Mockito) + rapport de couverture JaCoCo
                sh 'mvn -B test jacoco:report'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('MVN SONARQUBE') {
            steps {
                // Analyse statique + couverture JaCoCo (target/site/jacoco/jacoco.xml)
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar \
                          -Dsonar.host.url=http://sonarqube:9000 \
                          -Dsonar.token=$SONAR_TOKEN \
                          -Dsonar.projectKey=student-management \
                          -Dsonar.projectName=student-management
                    '''
                }
            }
        }

        stage('PUBLISH TO NEXUS') {
            steps {
                // Publie le .jar dans Nexus (version SNAPSHOT -> depot maven-snapshots)
                withCredentials([usernamePassword(credentialsId: 'nexus-cred',
                                                  usernameVariable: 'NEXUS_USER',
                                                  passwordVariable: 'NEXUS_PASS')]) {
                    // settings.xml temporaire : les ids correspondent a <distributionManagement> du pom
                    writeFile file: 'nexus-settings.xml', text: '''<settings>
  <servers>
    <server><id>nexus-snapshots</id><username>${env.NEXUS_USER}</username><password>${env.NEXUS_PASS}</password></server>
    <server><id>nexus-releases</id><username>${env.NEXUS_USER}</username><password>${env.NEXUS_PASS}</password></server>
  </servers>
</settings>'''
                    sh 'mvn -B deploy -DskipTests -s nexus-settings.xml -Daether.connector.http.preemptiveAuth=true'
                }
            }
            post {
                always {
                    sh 'rm -f nexus-settings.xml'
                }
            }
        }

        stage('BUILDING OUR IMAGE') {
            steps {
                // Image taguee avec le numero du build + "latest"
                withCredentials([usernamePassword(credentialsId: 'dockerhub-cred',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh 'docker build -t $DOCKER_USER/$IMAGE_NAME:$BUILD_NUMBER -t $DOCKER_USER/$IMAGE_NAME:latest .'
                }
            }
        }

        stage('DEPLOY OUR IMAGE') {
            steps {
                // Push de l'image sur Docker Hub
                withCredentials([usernamePassword(credentialsId: 'dockerhub-cred',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKER_USER/$IMAGE_NAME:$BUILD_NUMBER
                        docker push $DOCKER_USER/$IMAGE_NAME:latest
                        docker logout
                    '''
                }
            }
        }

        stage('TEST AWS CREDENTIALS') {
            steps {
                // Identifiants du Learner Lab (a mettre a jour a chaque session)
                withCredentials(awsCreds()) {
                    sh 'aws sts get-caller-identity'
                }
            }
        }

        stage('SETUP TERRAFORM') {
            steps {
                withCredentials(awsCreds()) {
                    dir('terraform') {
                        // Etat Terraform garde dans le volume Jenkins (survit aux builds)
                        sh '''
                            mkdir -p /var/jenkins_home/terraform-state
                            terraform init -input=false -reconfigure \
                              -backend-config="path=/var/jenkins_home/terraform-state/$CLUSTER_NAME.tfstate"
                            terraform apply -auto-approve -input=false \
                              -var="region=$AWS_REGION" \
                              -var="cluster_name=$CLUSTER_NAME"
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminee avec succes'
        }
        failure {
            echo 'Pipeline en echec : voir les logs de l etape en rouge'
        }
    }
}

// Identifiants AWS du Learner Lab :
//  - 'aws-creds'         : credential de type "AWS Credentials" (plugin AWS Credentials) -> access key + secret key
//  - 'aws-session-token' : credential "Secret text" -> session token (le Learner Lab donne des identifiants temporaires)
def awsCreds() {
    return [
        [$class: 'AmazonWebServicesCredentialsBinding',
         credentialsId: 'aws-creds',
         accessKeyVariable: 'AWS_ACCESS_KEY_ID',
         secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
        string(credentialsId: 'aws-session-token', variable: 'AWS_SESSION_TOKEN')
    ]
}
