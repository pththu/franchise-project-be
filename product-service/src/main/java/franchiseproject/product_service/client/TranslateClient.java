package franchiseproject.product_service.client;

import franchiseproject.product_service.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class TranslateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${application.ai.translate.url:http://127.0.0.1:3012}")
    private String baseUrl;

    public List<String> translate(List<String> texts) {

        String url = baseUrl + "/api/ai/translate";

        Map<String, Object> body = Map.of(
                "text", texts
        );

        try {
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(body),
                            new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {}
                    );

            if (response.getBody() != null &&
                    response.getBody().getData() != null) {

                return (List<String>) response.getBody().getData().get("text");
            }

        } catch (Exception e) {
            System.err.println("Translate error: " + e.getMessage());
        }

        return List.of();
    }
}