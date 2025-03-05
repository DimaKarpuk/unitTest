pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
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

            // 🔹 Отправка уведомления в Telegram
            script {
                def telegramToken = '7245091133:AAEWBoHTgfCn6vfUM6oaY41IMpdTdT5cmtc'
                def chatId = '-1002178373601'
                def allureReportUrl = "${env.BUILD_URL}allure"
                def buildStatus = currentBuild.result ?: 'SUCCESS'
                def message = "🚀 *Jenkins Build #${env.BUILD_NUMBER}*\n" +
                        "📌 *Status:* ${buildStatus}\n" +
                        "🔗 *Allure Report:* [Open Report](${allureReportUrl})\n" +
                        "📅 *Date:* ${new Date().format('yyyy-MM-dd HH:mm:ss')}"

                def command = "curl -s -X POST https://api.telegram.org/bot${telegramToken}/sendMessage " +
                        "-d chat_id=${chatId} " +
                        "-d parse_mode=Markdown " +
                        "-d text='${message}'"

                bat command
            }
        }
    }
}


