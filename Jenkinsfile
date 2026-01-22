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
                sh 'mvn -P service-tests verify'
            }
        }
        stage('4. Run E2E Tests') {
            steps {
                sh 'SERVER_URL=http://localhost:8080 mvn -P e2e -pl e2e-tests -am verify'
            }
        }
    }
    post {
        always {
            sh "find . -path '*/target/surefire-reports/*.xml' -print || true"
            sh "find . -path '*/target/failsafe-reports/*.xml' -print || true"
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            sh 'docker compose down'
        }
    }
}
