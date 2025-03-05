pipeline {
    agent any

    triggers {
        pollSCM('* * * * *') // Проверка изменений каждую минуту
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

            script {
                // Читаем конфиг из JSON
                def configFile = readFile('notifications/config.json')
                def config = new groovy.json.JsonSlurper().parseText(configFile)

                // Данные из конфига
                def telegramToken = config.telegram.token
                def chatId = config.telegram.chat
                def project = config.base.project
                def environment = config.base.environment
                def comment = config.base.comment
                def allureReportUrl = "${env.BUILD_URL}allure"
                def buildStatus = currentBuild.result ?: 'SUCCESS'

                // Формируем сообщение
                def message = "🚀 *Jenkins Build #${env.BUILD_NUMBER}*\n" +
                        "📌 *Project:* ${project}\n" +
                        "🌍 *Environment:* ${environment}\n" +
                        "💬 *Comment:* ${comment}\n" +
                        "📌 *Status:* ${buildStatus}\n" +
                        "🔗 *Allure Report:* [Open Report](${allureReportUrl})\n" +
                        "📅 *Date:* ${new Date().format('yyyy-MM-dd HH:mm:ss')}"

                // Команда для отправки в Telegram
                def command = "curl -s -X POST https://api.telegram.org/bot${telegramToken}/sendMessage " +
                        "-d chat_id=${chatId} " +
                        "-d parse_mode=MarkdownV2 " +
                        "-d text='${message}'"

                bat command
            }

        }
    }
}


