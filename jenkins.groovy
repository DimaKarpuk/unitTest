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
                // Чтение JSON файла
                def configFilePath = 'notifications/config.json'
                def jsonContent = readFile(configFilePath) // Читаем файл как строку
                def configData = readJSON text: jsonContent // Преобразуем в JSON объект

                // Конвертируем JSON объект в обычный HashMap
                def serializableConfig = configData.collectEntries { key, value -> [key, value] }

                // Использование данных
                def telegramToken = serializableConfig.telegram.token
                def chatId = serializableConfig.telegram.chat
                def message = serializableConfig.base.comment

                // Отправка сообщения
                def command = "curl -s -X POST https://api.telegram.org/bot${telegramToken}/sendMessage " +
                        "-d chat_id=${chatId} " +
                        "-d text=\"${message}\""
                bat command
            }

        }
    }
}


