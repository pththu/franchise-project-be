package franchiseproject.product_service.client;

import franchiseproject.product_service.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class TranslateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${application.ai.translate.url:http://127.0.0.1:3012}")
    private String baseUrl;

    @Value("${application.ai.translate.languages:en, jp}")
    private String configuredLanguages;

    public List<String> translate(List<String> texts) {

        List<String> languages = parseLanguages(configuredLanguages);
        Map<String, List<String>> translatedByLanguage = translateByLanguage(texts, languages);

        List<String> flattened = new ArrayList<>();
        for (String language : languages) {
            flattened.addAll(translatedByLanguage.getOrDefault(language, List.of()));
        }

        return flattened;
    }

    public Map<String, List<String>> translateByLanguage(List<String> texts, List<String> languages) {

        log.info("Start translate");

        if (texts == null || texts.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> normalizedLanguages = normalizeLanguages(languages);
        if (normalizedLanguages.isEmpty()) {
            normalizedLanguages = List.of("en", "ja");
        }

        log.info("normalizedLanguages: {}", normalizedLanguages);

        String url = baseUrl + "/api/ai/translate";

        Map<String, Object> body = new HashMap<>();
        body.put("text_list", texts);
        body.put("language", String.join(", ", normalizedLanguages));

        try {
            log.info("url: {}", url);
            ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                    });

            log.info("response: {}", response);

            if (response.getBody() != null) {
                List<String> translatedTexts = extractTranslatedTexts(response.getBody().getData());
                int chunkSize = texts.size();
                int expectedSize = chunkSize * normalizedLanguages.size();

                if (translatedTexts.size() < expectedSize) {
                    throw new IllegalStateException("Invalid translation size. expected=" + expectedSize + ", actual="
                            + translatedTexts.size());
                }

                Map<String, List<String>> result = new LinkedHashMap<>();
                for (int i = 0; i < normalizedLanguages.size(); i++) {
                    int start = i * chunkSize;
                    int end = start + chunkSize;
                    result.put(normalizedLanguages.get(i), translatedTexts.subList(start, end));
                }
                return result;
            }
        } catch (Exception e) {
            log.error("Translate error: {}", e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    private List<String> extractTranslatedTexts(Object data) {
        if (data == null) {
            return List.of();
        }

        if (data instanceof List<?> listData) {
            return listData.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .toList();
        }

        if (data instanceof Map<?, ?> mapData) {
            Object textObject = mapData.get("text");
            if (textObject instanceof List<?> textList) {
                return textList.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .toList();
            }
        }

        return List.of();
    }

    private List<String> parseLanguages(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("en", "ja");
        }

        return normalizeLanguages(List.of(raw.split(",")));
    }

    private List<String> normalizeLanguages(List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return List.of();
        }

        return languages.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .map(code -> code.equals("jp") ? "ja" : code)
                .distinct()
                .toList();
    }
}