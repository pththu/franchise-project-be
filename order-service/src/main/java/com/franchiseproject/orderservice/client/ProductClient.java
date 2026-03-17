package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.CreateOrderItemRequest;
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

    //Mock Test
    List<ProductResponse> items = List.of(
            new ProductResponse(UUID.fromString("11111111-aaaa-1111-1111-111111111111"), "Coffee Bean", BigDecimal.valueOf(22000.00), "ACTIVE"),
            new ProductResponse(UUID.fromString("22222222-bbbb-2222-2222-222222222222"), "Milk", BigDecimal.valueOf(55000.00), "ACTIVE"),
            new ProductResponse(UUID.fromString("33333333-cccc-3333-3333-333333333333"), "Orange Juice", BigDecimal.valueOf(20000.00), "ACTIVE"),
            new ProductResponse(UUID.fromString("44444444-dddd-4444-4444-444444444444"), "Coca Cola", BigDecimal.valueOf(232000.00), "ACTIVE"),
            new ProductResponse(UUID.fromString("55555555-eeee-5555-5555-555555555555"), "Pepsi", BigDecimal.valueOf(721000.00), "ACTIVE"),
            new ProductResponse(UUID.fromString("66666666-ffff-6666-6666-666666666666"), "Pepsi", BigDecimal.valueOf(92000.00), "ACTIVE")
    );

    public Map<UUID, ProductResponse> getProductsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.NO_PRODUCTS);
        }
        List<UUID> listId = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // MOCK DATA
        List<ProductResponse> products = items.stream()
                .filter(p -> listId.contains(p.getId()))
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
