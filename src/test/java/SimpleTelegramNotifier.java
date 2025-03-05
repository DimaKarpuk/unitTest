import org.junit.jupiter.api.AfterAll;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleTelegramNotifier {

    @AfterAll
    public static void sendMessage() {
        // Укажите переменные
        String token = "7245091133:AAEWBoHTgfCn6vfUM6oaY41IMpdTdT5cmtc";
        String chatId = "-1002178373601";
        String message = "Pipeline completed successfully!";
        String reportLink = "http://localhost:8080/job/unitTests/allure-report";

        // Формирование сообщения
        String fullMessage = message + "\n\n" + "🔗 Allure Report: " + reportLink;

        // Формирование URL для Telegram API
        String telegramApiUrl = "https://api.telegram.org/bot" + token + "/sendMessage";

        try {
            // Отправка сообщения
            sendMessage(telegramApiUrl, chatId, fullMessage);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendMessage(String apiUrl, String chatId, String message) throws Exception {
        // Формирование тела POST-запроса
        String payload = String.format("{\"chat_id\": \"%s\", \"text\": \"%s\"}", chatId, message);

        // Установка соединения
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // Запись данных в запрос
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes("UTF-8"));
        }

        // Проверка ответа
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Message sent successfully!");
        } else {
            System.err.println("Failed to send message. HTTP response code: " + responseCode);
        }
    }
}
