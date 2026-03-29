package com.franchiseproject.customerservice.client;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.response.UserResponse;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
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
public class IdentityClient {
    final RestTemplate restTemplate;

    @Value("${application.feign.identity-access-service.url:http://localhost:3004}")
    String identityServiceBaseUrl;

    public UserResponse getUserById(UUID userId) {
        if (userId == null) return null;
        String url = identityServiceBaseUrl + "/api/auth/internal/users/" + userId;
        try {
            ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<UserResponse>>() {
                    }
            );
            return response.getBody() != null ? response.getBody().getData() : null;
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public List<UserResponse> getUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();
        String url = identityServiceBaseUrl + "/api/auth/internal/users/search-by-ids";
        try {
            HttpEntity<List<UUID>> entity = new HttpEntity<>(userIds);
            ResponseEntity<ApiResponse<List<UserResponse>>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ApiResponse<List<UserResponse>>>() {
                    }
            );
            return response.getBody() != null ? response.getBody().getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Bulk fetch users failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}