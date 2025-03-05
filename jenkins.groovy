pipeline {
    agent any

    triggers {
        pollSCM('* * * * *') // Проверяет изменения каждую минуту
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
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    bat 'gradlew clean build'
                }
            }
        }

        stage('Test') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    bat 'gradlew test'
                }
            }
        }

        stage('Allure Report') {
            steps {
                script {
                    if (fileExists('build/allure-results')) {
                        bat 'gradlew allureReport'
                        allure includeProperties: false, jdk: '', results: [[path: 'build/allure-results']]
                    } else {
                        echo 'No test results found. Skipping Allure report generation.'
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

            echo 'Sending notification to Telegram...'
            script {
                // Определяем статус выполнения pipeline
                def status = currentBuild.result ?: 'SUCCESS'
                def comment = ''
                if (status == 'SUCCESS') {
                    comment = "Pipeline completed successfully. All tests passed. 🎉"
                } else if (status == 'UNSTABLE') {
                    comment = "Pipeline completed with unstable status. Some tests failed. ⚠️"
                } else {
                    comment = "Pipeline failed. Please check the logs for more details. ❌"
                }

                // Генерация JSON для отправки в Telegram
                def config = """
                {
                    "base": {
                        "logo": "",
                        "project": "${env.JOB_NAME}",
                        "environment": "Test Environment",
                        "comment": "${comment}",
                        "reportLink": "${env.BUILD_URL}",
                        "language": "en",
                        "allureFolder": "allure-report",
                        "enableChart": true
                    },
                    "telegram": {
                        "token": "7245091133:AAEWBoHTgfCn6vfUM6oaY41IMpdTdT5cmtc",
                        "chat": "-1002178373601",
                        "replyTo": ""
                    }
                }
                """
                writeFile file: 'config.json', text: config
                bat 'curl -X POST -H "Content-Type: application/json" -d @config.json https://your-telegram-notification-endpoint'
            }
        }
    }
}
