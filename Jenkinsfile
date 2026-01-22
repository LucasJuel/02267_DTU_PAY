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
                echo "System started."
            }
        }
        stage('2.1 Wait for Services') {
            steps {
                sh '''
                    set -e
                    wait_for_health() {
                        name="$1"
                        timeout="${2:-60}"
                        echo "Waiting for ${name} to be healthy..."
                        for i in $(seq 1 "$timeout"); do
                            status=$(docker inspect -f '{{.State.Health.Status}}' "$name" 2>/dev/null || echo "unknown")
                            if [ "$status" = "healthy" ]; then
                                echo "${name} is healthy."
                                return 0
                            fi
                            sleep 1
                        done
                        echo "${name} is not healthy after ${timeout}s."
                        docker logs "$name" || true
                        return 1
                    }
                    wait_for_health rabbitmq 60
                    wait_for_health account-service 60
                    wait_for_health payment-service 60
                    echo "System is healthy and ready for testing."
                '''
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
