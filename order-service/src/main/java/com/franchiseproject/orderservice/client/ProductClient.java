package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.CreateOrderItemRequest;
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

//    public Map<UUID, ProductResponse> getProductsByIds(List<UUID> ids) {
//
//        List<ProductResponse> products = productRestClient.post()
//                .uri("/api/products/batch")
//                .body(ids)
//                .retrieve()
//                .body(new ParameterizedTypeReference<>() {
//                });
//        if (products == null || products.isEmpty()) {
//            throw new AppException(ErrorCode.NO_PRODUCTS);
//        }
//        return products.stream()
//                .collect(Collectors.toMap(ProductResponse::getId, p -> p));
//    }

//    public BigDecimal validateAndCalculate(UUID customerId, UUID promotionId, BigDecimal totalItems) {
//
//        DiscountRequest request = new DiscountRequest(customerId, promotionId, totalItems);
//
//        BigDecimal discount = productRestClient.post()
//                .uri("/api/promotions/validate")
//                .body(request)
//                .retrieve()
//                .body(BigDecimal.class);
//        return Optional.ofNullable(discount)
//                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_FAILED));
//    }


    public Map<UUID, ProductResponse> getProductsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.NO_PRODUCTS);
        }
        List<UUID> listId = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // MOCK DATA - Generate dynamically to avoid 404 during testing
        List<ProductResponse> products = listId.stream()
                .map(id -> new ProductResponse(id, "Mock Product " + id.toString().substring(0, 8), BigDecimal.valueOf(50000.00), "ACTIVE"))
                .toList();
        return products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));
    }

    public BigDecimal validateAndCalculate(UUID customerId,
                                           UUID promotionId,
                                           BigDecimal totalItems) {
        if (promotionId == null) {
            return BigDecimal.ZERO;
        }
        // Giả lập giảm 10%
        return totalItems.multiply(BigDecimal.valueOf(0.1));
    }

}
