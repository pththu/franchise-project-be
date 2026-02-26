package com.franchiseproject.orderservice.infrastructure.client;

import com.franchiseproject.orderservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductClient {
    private final RestClient productRestClient;

    public ProductResponse getProductById(UUID id) {
        return productRestClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .body(ProductResponse.class);
    }
}
