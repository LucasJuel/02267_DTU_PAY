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
        stage('3. Run Maven Test Suite') {
            steps {
                script {
                    try {
                        sh 'sleep 15 && cd Client/dtu-pay-client && mvn clean test'
                    } catch (Exception e) {
                        sh 'docker compose logs --tail=50 server'
                        error "Tests failed. Check server logs above."
                    }
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