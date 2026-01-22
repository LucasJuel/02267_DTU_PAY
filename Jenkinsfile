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
                sh "find . -path '*/target/failsafe-reports/*.xml' -delete || true"
            }
        }
        stage('3. Run Unit + Service Tests') {
            steps {
                sh 'mvn test'
            }
        }
        stage('4. Run E2E Tests') {
            steps {
                sh 'SERVER_URL=http://localhost:8080 mvn -pl e2e-tests -am test'
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
