pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
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
