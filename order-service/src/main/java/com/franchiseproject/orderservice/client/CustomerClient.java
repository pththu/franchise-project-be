package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.CustomerResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerClient {

    public CustomerResponse getCustomerById(UUID id) {
        if (id == null) return null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = null;
            Cookie[] cookies = null;
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                token = request.getHeader("Authorization");
                cookies = request.getCookies();
            }

            var spec = RestClient.builder()
                    .baseUrl("http://localhost:3004")
                    .build()
                    .get()
                    .uri("/api/auth/users/" + id);
            if (token != null) {
                spec.header("Authorization", token);
            }
            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (var c : cookies) {
                    cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                }
                spec.header("Cookie", cookieBuilder.toString());
            }

            ApiResponse<CustomerResponse> response = spec.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<CustomerResponse>>() {});
            
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error fetching customer for ID {}: {}", id, e.getMessage(), e);
            // Fallback cleanly if remote API fails or 403/404
            return null;
        }
    }

    public List<UUID> searchCustomerIdsByKeyword(String keyword) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            Cookie[] cookies = attributes != null ? attributes.getRequest().getCookies() : null;

            var spec = RestClient.builder().baseUrl("http://localhost:3004").build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users/search")
                            .queryParam("size", 50)
                            .queryParam("keyword", keyword)
                            .build());

            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (var c : cookies) {
                    cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                }
                spec.header("Cookie", cookieBuilder.toString());
            }

            var res = spec.retrieve().body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
            if (res != null && res.getData() != null) {
                Object content = res.getData().get("content");
                if (content instanceof List) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) content;
                    log.info("CustomerClient: search API found {} matching customer accounts", list.size());
                    return list.stream()
                            .map(m -> m.get("id"))
                            .filter(java.util.Objects::nonNull)
                            .map(id -> UUID.fromString(id.toString()))
                            .collect(java.util.stream.Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("Error searching customers by keyword {}: {}", keyword, e.getMessage());
        }
        return java.util.Collections.emptyList();
    }

    public Map<UUID, CustomerResponse> getCustomersByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            Cookie[] cookies = attributes != null ? attributes.getRequest().getCookies() : null;

            var spec = RestClient.builder().baseUrl("http://localhost:3004").build()
                    .post()
                    .uri("/api/auth/users/bulk");

            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (var c : cookies) {
                    cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                }
                spec.header("Cookie", cookieBuilder.toString());
            }

            ApiResponse<java.util.List<CustomerResponse>> res = spec.body(ids)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<java.util.List<CustomerResponse>>>() {});

            if (res != null && res.getData() != null) {
                return res.getData().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                CustomerResponse::getId,
                                c -> c,
                                (existing, replacing) -> existing // Handle duplicates if any
                        ));
            }
        } catch (Exception e) {
            log.error("Error bulk fetching customers for IDs {}: {}", ids, e.getMessage());
        }
        return java.util.Collections.emptyMap();
    }
}
