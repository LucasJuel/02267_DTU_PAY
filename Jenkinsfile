pipeline {
    agent any
    stages {
        stage('1. Cleanup & Build') {
            steps {
                sh 'docker compose down --remove-orphans || true'
                sh 'docker compose build'
            }
        }
        stage('2. Start System') {
            steps {
                sh 'docker compose up -d'
                echo "System is healthy and ready for testing."
            }
        }
        stage('3. Run Maven Tests (All Services)') {
            steps {
                dir('Account/account-service') {
                    sh 'mvn test'
                }
                dir('Payment/payment-service') {
                    sh 'mvn test'
                }
                dir('APIGateway/api-gateway') {
                    sh 'mvn test'
                }
                dir('Server/thebois-dtu-pay') {
                    sh 'mvn test'
                }
            }
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            sh 'docker compose down'
        }
    }
}
