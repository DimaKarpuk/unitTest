pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *') // Проверка изменений каждые 5 минут
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out the repository...'
                git credentialsId: 'github-token', url: 'https://github.com/DimaKarpuk/unitTest.git'
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project...'
                bat 'gradlew clean build'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                // Обработка ошибок для продолжения выполнения, даже если тесты упали
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    bat 'gradlew test'
                }
            }
        }

        stage('Allure Report') {
            steps {
                echo 'Generating Allure report...'
                script {
                    if (fileExists('build/allure-results')) {
                        bat 'gradlew allureReport' // Генерация отчета Allure
                        allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
                    } else {
                        echo 'No test results found, skipping Allure report generation.'
                    }
                }
            }
        }

        stage('Report') {
            steps {
                echo 'Publishing JUnit test report...'
                junit '**/build/test-results/test/*.xml'
            }
        }
    }

    post {
        always {
            echo 'Archiving artifacts...'
            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
            archiveArtifacts artifacts: '**/build/test-results/test/*.xml', fingerprint: true
            archiveArtifacts artifacts: '**/build/reports/allure-report/**', fingerprint: true
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        unstable {
            echo 'Pipeline is unstable due to failed tests.'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for more details.'
        }
    }
}
