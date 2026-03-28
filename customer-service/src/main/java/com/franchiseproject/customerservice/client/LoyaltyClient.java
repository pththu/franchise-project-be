package com.franchiseproject.customerservice.client;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.response.CustomerTierResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyClient {
    final RestTemplate restTemplate;

    @Value("${application.feign.loyalty-service.url:http://localhost:3005}")
    String loyaltyServiceBaseUrl;

    public CustomerTierResponse getCustomerTierInfo(UUID userId, UUID franchiseId) {
        if (userId == null || franchiseId == null) return null;

        String url = loyaltyServiceBaseUrl + "/api/loyalty/customers/" + userId + "/franchises/" + franchiseId + "/tier-info";

        try {
            log.info("Calling Loyalty API: {}", url);
            ResponseEntity<ApiResponse<CustomerTierResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<CustomerTierResponse>>() {
                    }
            );
            return response.getBody() != null ? response.getBody().getData() : null;
        } catch (Exception e) {
            log.error("Failed to fetch loyalty info for user: {} at franchise: {}", userId, franchiseId, e);
            return null;
        }
    }

    public List<CustomerTierResponse> getBulkCustomerTierInfo(List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) return Collections.emptyList();

        String url = loyaltyServiceBaseUrl + "/api/loyalty/internal/customers/bulk-tier-info";

        try {
            ResponseEntity<ApiResponse<List<CustomerTierResponse>>> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(customerIds),
                    new ParameterizedTypeReference<ApiResponse<List<CustomerTierResponse>>>() {
                    }
            );
            return response.getBody() != null ? response.getBody().getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch bulk loyalty info", e);
            return Collections.emptyList();
        }
    }
}