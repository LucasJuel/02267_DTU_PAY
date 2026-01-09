pipeline {
    agent any
    stages {
        stage('Cleanup Environment') {
            steps {
                // Wipe old containers to free up ports 8080/8181
                sh 'docker compose down --remove-orphans || true'
            }
        }
        stage('Build Images') {
            steps {
                // Build the lean runtime images
                sh 'docker compose build'
            }
        }
        stage('Run System & Tests') {
            steps {
                script {
                    try {
                        // 1. Start Quarkus Server using the key 'server'
                        sh 'docker compose up -d server'

                        // 2. Wait for Quarkus to boot (Essential for JVM mode)
                        echo "Waiting for Quarkus to boot..."
                        sh 'sleep 15'

                        // 3. Start 'client' and override entrypoint to run tests
                        // Using 'run --rm' ensures the test container is removed after finishing
                        sh 'docker compose run --rm client mvn test'
                    } finally {
                        // 4. Always tear down to keep the Jenkins node clean
                        sh 'docker compose down'
                    }
                }
            }
        }
    }
    post {
        always {
            // Jenkins reads the reports mapped via the volume in your docker-compose
            junit '**/target/surefire-reports/*.xml'
        }
    }
}