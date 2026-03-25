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
        if (userId == null) {
            return null;
        }

        String url = identityServiceBaseUrl + "/api/auth/internal/users/" + userId;

        try {
            log.info("Calling Identity API: {}", url);
            ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<UserResponse>>() {}
            );
            return response.getBody().getData();
        } catch (Exception e) {
            // Master Tip: In ra toàn bộ lỗi để biết nó là 404, 500 hay lỗi Parse JSON
            log.error("REST_CALL_ERROR | URL: {} | Error: {}", url, e.getMessage(), e);
            return null;
        }
    }

    public List<UserResponse> getUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();

        // Giả sử identity-service có endpoint: POST /api/auth/users/search-by-ids
        String url = identityServiceBaseUrl + "/api/auth/internal/users/search-by-ids";
        try {
            ResponseEntity<ApiResponse<List<UserResponse>>> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(userIds),
                    new ParameterizedTypeReference<ApiResponse<List<UserResponse>>>() {
                    }
            );
            return (response.getBody() != null) ? response.getBody().getData() : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Bulk fetch users failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
