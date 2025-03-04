pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/DimaKarpuk/WireMockTest.git'
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew clean build'
            }
        }

        stage('Test in Docker') {
            steps {
                script {
                    sh 'docker-compose up --build -d'
                }
            }
        }

        stage('Allure Report') {
            steps {
                bat 'gradlew allureReport'
                allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
            }
        }

        stage('Report') {
            steps {
                junit '**/build/test-results/test/*.xml'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
            archiveArtifacts artifacts: '**/build/test-results/test/*.xml', fingerprint: true
            archiveArtifacts artifacts: '**/build/reports/allure-report/**', fingerprint: true
            script {
                sh 'docker-compose down'
            }
        }
    }
}



