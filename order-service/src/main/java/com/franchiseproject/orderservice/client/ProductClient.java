package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductClient {
    private final RestClient apiProductRestClient;

    @org.springframework.beans.factory.annotation.Value("${application.product-service.url}")
    private String productServiceUrl;

    public Map<UUID, ProductResponse> getProductsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.NO_PRODUCTS);
        }
 
        try {
            var spec = RestClient.builder()
                    .baseUrl(productServiceUrl)
                    .build()
                    .post()
                    .uri("/api/products/variants/bulk")
                    .body(ids);
 
            addAuthHeaders(spec);
 
            ApiResponse<List<Map<String, Object>>> response = spec.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {});

            if (response == null || response.getData() == null) {
                throw new AppException(ErrorCode.NO_PRODUCTS);
            }

            return response.getData().stream()
                    .map(map -> ProductResponse.builder()
                            .id(UUID.fromString(map.get("id").toString()))
                            .name(map.get("productName") != null ? map.get("productName").toString() : "Unknown Product")
                            .price(map.get("price") != null ? new BigDecimal(map.get("price").toString()) : BigDecimal.ZERO)
                            .status("ACTIVE")
                            .imageUrl(map.get("imageUrl") != null ? map.get("imageUrl").toString() : null)
                            .build())
                    .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        } catch (Exception e) {
            log.error("Error fetching products from product-service: {}", e.getMessage());
            throw new AppException(ErrorCode.NO_PRODUCTS);
        }
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

    private void addAuthHeaders(RestClient.RequestBodySpec spec) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            if (token != null) {
                spec.header("Authorization", token);
            }
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (Cookie cookie : cookies) {
                    cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                }
                spec.header("Cookie", cookieBuilder.toString());
            }
        }
    }

}
