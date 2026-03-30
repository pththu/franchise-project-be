package com.franchiseproject.customerservice.client;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.response.FranchiseResponse;
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
public class FranchiseClient {
    final RestTemplate restTemplate;

    @Value("${application.feign.franchise-service.url:http://localhost:3013}")
    String franchiseServiceBaseUrl;

    public List<FranchiseResponse> getFranchisesByIds(List<UUID> franchiseIds) {
        if (franchiseIds == null || franchiseIds.isEmpty()) return Collections.emptyList();

        String url = franchiseServiceBaseUrl + "/api/franchises/internal/search-by-ids";
        try {
            ResponseEntity<ApiResponse<List<FranchiseResponse>>> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(franchiseIds),
                    new ParameterizedTypeReference<ApiResponse<List<FranchiseResponse>>>() {
                    }
            );
            return (response.getBody() != null) ? response.getBody().getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Bulk fetch franchises failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}