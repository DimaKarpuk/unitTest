pipeline {
    agent any


    triggers {
        genericTrigger(
                genericVariables: [
                        [key: 'pull_request', value: '$.action'],
                        [key: 'branch_name', value: '$.pull_request.head.ref']
                ],
                causeString: 'Triggered on Pull Request',
                token: 'YOUR_SECRET_TOKEN'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-token', url: 'https://github.com/DimaKarpuk/unitTest.git'
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew clean build'
            }
        }

        stage('Test') {
            steps {
                bat 'gradlew test'
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
        }
    }
}

