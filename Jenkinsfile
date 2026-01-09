pipeline {
    agent any
    stages {
        stage('Cleanup Environment') {
            steps {
                // Wipe old containers so port 8080/8181 is free
                sh 'docker compose down --remove-orphans || true'
            }
        }
        stage('Build Images') {
            steps {
                // Build your lean Version 2 images
                sh 'docker compose build'
            }
        }
        stage('Run System & Tests') {
            steps {
                script {
                    try {
                        // 1. Start Quarkus Server
                        sh 'docker compose up -d dtu-pay-server'

                        // 2. Wait for Quarkus to boot (Essential for JVM mode)
                        echo "Waiting for Quarkus to boot..."
                        sh 'sleep 15'

                        // 3. Override the Version 2 Entrypoint to run tests
                        // This starts the client container but runs 'mvn test' inside it
                        sh 'docker compose run --rm client mvn test'
                    } finally {
                        // 4. Always tear down to keep the server clean
                        sh 'docker compose down'
                    }
                }
            }
        }
    }
    post {
        always {
            // Jenkins will look for the XML reports created by 'mvn test'
            junit '**/target/surefire-reports/*.xml'
        }
    }
}