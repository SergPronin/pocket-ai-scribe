package ru.pronin_s_v.pocketaiscribe.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.pronin_s_v.pocketaiscribe.core.port.AudioTranscriber;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SaluteSpeechTranscriber implements AudioTranscriber {

    private final RestClient restClient;
    private final String authKey;
    private final String speechUrl;
    private final String scope;

    public SaluteSpeechTranscriber(
        @Value("${sber.auth.key}") String authKey,
        @Value("${sber.speech.url}") String speechUrl,
        @Value("${sber.speech.scope}") String scope,
        RestClient.Builder restClientBuilder) {
        this.authKey = authKey;
        this.speechUrl = speechUrl;
        this.scope = scope;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String transcribe(byte[] audioData) {
        String accessToken = getAccessToken();

        Map<?, ?> response = restClient.post()
            .uri(speechUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .header(HttpHeaders.CONTENT_TYPE, "audio/x-pcm;bit=16;rate=16000")
            .body(audioData)
            .retrieve()
            .body(Map.class);

        if (response != null && response.containsKey("result")) {
            Object result = response.get("result");
            if (result instanceof List<?> results && !results.isEmpty() && results.get(0) instanceof String firstResult) {
                return firstResult;
            }
        }

        throw new RuntimeException("Ошибка распознавания речи через SaluteSpeech");
    }

    private String getAccessToken() {
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalStateException("SBER_AUTH_KEY is empty. Fill it in .env or environment variables.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("scope", scope);

        Map<?, ?> response;
        try {
            response = restClient.post()
                .uri("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + authKey)
                .header("RqUID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalStateException(
                "Sber OAuth returned 401 Unauthorized. Check SBER_AUTH_KEY value/validity and access rights for scope " + scope + ".",
                e
            );
        }

        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }

        throw new RuntimeException("Не удалось получить access_token: " + response);
    }
}