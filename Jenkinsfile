pipeline {
    agent any
    stages {
        stage('Cleanup Environment') {
            steps {
                // Force remove any old containers/networks associated with this project
                sh 'docker compose down --remove-orphans || true'
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
                        echo "Waiting for Quarkus to boot..."
                        sh 'sleep 15'

                        // Run tests in the live container
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