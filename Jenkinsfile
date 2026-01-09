pipeline {
    agent any
    stages {
        stage('1. Environment Cleanup') {
            steps {
                sh 'docker compose down --remove-orphans || true'
            }
        }
        stage('2. Build Images') {
            steps {
                sh 'docker compose build'
            }
        }
        stage('3. Start System') {
            steps {
                sh 'docker compose up -d'
                echo "Waiting for Quarkus server to initialize..."
                sh 'sleep 15'
            }
        }
        stage('4. Run Maven Test Suite') {
            steps {
                sh 'docker compose run --rm client mvn test'
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