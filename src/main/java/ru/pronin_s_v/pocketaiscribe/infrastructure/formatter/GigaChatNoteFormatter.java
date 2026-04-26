package ru.pronin_s_v.pocketaiscribe.infrastructure.formatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.pronin_s_v.pocketaiscribe.core.port.NoteFormatter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Primary // Spring выберет этот класс вместо старого SimpleNoteFormatter
public class GigaChatNoteFormatter implements NoteFormatter {

    private final RestClient restClient;
    private final String authKey;
    private final String scope;
    private final String chatUrl;

    public GigaChatNoteFormatter(
            @Value("${sber.gigachat.auth.key}") String authKey,
            @Value("${sber.gigachat.scope}") String scope,
            @Value("${sber.gigachat.url}") String chatUrl,
            RestClient.Builder restClientBuilder) {
        this.authKey = authKey;
        this.scope = scope;
        this.chatUrl = chatUrl;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String formatToMarkdown(String rawText) {
        String accessToken = getAccessToken();

        // Формируем системную инструкцию для нейросети
        String systemInstruction = """
                Ты — ассистент по ведению заметок в Obsidian. 
                Преврати сырой текст в структурированную заметку.
                Используй заголовки, списки и выдели ключевые задачи, если они есть.
                Верни только Markdown-код без лишних комментариев.
                """;

        Map<String, Object> body = Map.of(
                "model", "GigaChat",
                "messages", List.of(
                        Map.of("role", "system", "content", systemInstruction),
                        Map.of("role", "user", "content", "Оформи этот текст: " + rawText)
                )
        );

        Map<?, ?> response = restClient.post()
                .uri(chatUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractContent(response);
    }

    private String getAccessToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("scope", scope);

        Map<?, ?> response = restClient.post()
                .uri("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + authKey)
                .header("RqUID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }

    private String extractContent(Map<?, ?> response) {
        List<?> choices = (List<?>) response.get("choices");
        Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        return (String) message.get("content");
    }
}