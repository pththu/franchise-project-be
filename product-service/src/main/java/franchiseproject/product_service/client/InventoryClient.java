package franchiseproject.product_service.client;

import franchiseproject.product_service.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${application.feign.inventory-service.url:http://localhost:3002}")
    private String inventoryServiceUrl;

    public List<UUID> getInStockVariantIds(UUID locationId) {
        if (locationId == null) {
            return Collections.emptyList();
        }
        String url = inventoryServiceUrl + "/api/inventory/stocks/variants/in-stock?locationId=" + locationId;
        try {
            ResponseEntity<ApiResponse<List<UUID>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<UUID>>>() {}
            );
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            System.err.println("Error calling inventory-service: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
