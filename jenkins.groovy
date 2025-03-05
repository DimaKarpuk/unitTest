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



