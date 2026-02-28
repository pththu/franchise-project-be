package com.franchiseproject.orderservice.infrastructure.client;

import com.franchiseproject.orderservice.dto.request.DiscountRequest;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductClient {
    private final RestClient productRestClient;

    public Map<UUID, ProductResponse> getProductsByIds(List<UUID> ids) {

        List<ProductResponse> products = productRestClient.post()
                .uri("/api/products/batch")
                .body(ids)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (products == null || products.isEmpty()) {
            throw new AppException(ErrorCode.NO_PRODUCTS);
        }
        return products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));
    }

    public BigDecimal validateAndCalculate(UUID customerId, UUID promotionId, BigDecimal totalItems) {

        DiscountRequest request = new DiscountRequest(customerId, promotionId, totalItems);

        BigDecimal discount = productRestClient.post()
                .uri("/api/promotions/validate")
                .body(request)
                .retrieve()
                .body(BigDecimal.class);
        return Optional.ofNullable(discount)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_FAILED));
    }

}
