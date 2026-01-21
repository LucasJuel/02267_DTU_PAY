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
        stage('2.5 Cleanup Old Test Reports') {
            steps {
                sh "find . -path '*/target/surefire-reports/*.xml' -delete || true"
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
                dir('Client/dtu-pay-client') {
                    sh 'mvn test'
                }
            }
        }
    }
    post {
        always {
            sh "find . -path '*/target/surefire-reports/*.xml' -print || true"
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            sh 'docker compose down'
        }
    }
}
