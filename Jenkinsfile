pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Run System & Tests') {
            steps {
                script {
                    try {
                        sh 'docker compose up -d'

                        sh 'docker compose exec -T client mvn test'
                    } finally {
                        sh 'docker compose down'
                    }
                }
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}