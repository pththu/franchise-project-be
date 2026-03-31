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

    @SuppressWarnings("unchecked")
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
                    .baseUrl("http://localhost:3003")
                    .build()
                    .get()
                    .uri("/api/customers/" + id);
            if (token != null) {
                spec = spec.header("Authorization", token);
            }
            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (var c : cookies) {
                    cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                }
                spec = spec.header("Cookie", cookieBuilder.toString());
            }
            CustomerResponse res = null;
            ApiResponse<Map<String, Object>> response = spec.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
            
            if (response != null && response.getData() != null) {
                Map<String, Object> data = response.getData();
                res = new CustomerResponse();
                res.setId(UUID.fromString(data.get("id").toString()));
                
                // Robust parsing: check for nested userResponse OR top-level userId
                Map<String, Object> user = (Map<String, Object>) data.get("userResponse");
                if (user == null) user = (Map<String, Object>) data.get("user_response");

                if (user != null && (user.get("id") != null || user.get("userId") != null)) {
                    Object idObj = user.get("id") != null ? user.get("id") : user.get("userId");
                    res.setUserId(UUID.fromString(idObj.toString()));
                    
                    String fullName = (String) user.get("fullName");
                    if (fullName == null) fullName = (String) user.get("full_name");
                    res.setFullName(fullName);
                    
                    res.setEmail((String) user.get("email"));
                    res.setPhone((String) user.get("phone"));
                } else if (data.get("userId") != null || data.get("user_id") != null) {
                    Object userIdObj = data.get("userId") != null ? data.get("userId") : data.get("user_id");
                    res.setUserId(UUID.fromString(userIdObj.toString()));
                    
                    String fullName = (String) data.get("fullName");
                    if (fullName == null) fullName = (String) data.get("full_name");
                    res.setFullName(fullName);
                }
            }
            return res;
        } catch (Exception e) {
            log.warn("Could not fetch customer for ID {}: {}", id, e.getMessage());
            // Fallback cleanly if remote API fails or 403/404
            return null;
        }
    }

    public List<UUID> searchCustomerIdsByKeyword(String keyword, UUID franchiseId) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            Cookie[] cookies = attributes != null ? attributes.getRequest().getCookies() : null;

            // Step 1: Search Users in Identity Service
            var identitySpec = RestClient.builder().baseUrl("http://localhost:3004").build()
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
                identitySpec.header("Cookie", cookieBuilder.toString());
            }

            var identityRes = identitySpec.retrieve().body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
            if (identityRes != null && identityRes.getData() != null) {
                Object content = identityRes.getData().get("content");
                if (content instanceof List) {
                    List<Map<String, Object>> userList = (List<Map<String, Object>>) content;
                    List<UUID> userIds = userList.stream()
                            .map(m -> m.get("id"))
                            .filter(java.util.Objects::nonNull)
                            .map(id -> UUID.fromString(id.toString()))
                            .collect(java.util.stream.Collectors.toList());

                    if (userIds.isEmpty()) return Collections.emptyList();

                    // Step 2: Resolve User IDs to Customer IDs in Customer Service
                    var customerSpec = RestClient.builder().baseUrl("http://localhost:3003").build()
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/customers/search")
                                    .queryParam("franchiseId", franchiseId)
                                    .queryParam("userIds", userIds.stream().map(UUID::toString).collect(java.util.stream.Collectors.joining(",")))
                                    .build());

                    if (cookies != null) {
                        StringBuilder cookieBuilder = new StringBuilder();
                        for (var c : cookies) {
                            cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                        }
                        customerSpec.header("Cookie", cookieBuilder.toString());
                    }

                    var customerRes = customerSpec.retrieve().body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
                    if (customerRes != null && customerRes.getData() != null) {
                        Object customerContent = customerRes.getData().get("content");
                        if (customerContent instanceof List) {
                            List<Map<String, Object>> customerList = (List<Map<String, Object>>) customerContent;
                            return customerList.stream()
                                    .map(m -> m.get("id"))
                                    .filter(java.util.Objects::nonNull)
                                    .map(id -> UUID.fromString(id.toString()))
                                    .collect(java.util.stream.Collectors.toList());
                        }
                    }
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

            var spec = RestClient.builder().baseUrl("http://localhost:3003").build()
                    .post()
                    .uri("/api/customers/bulk");

            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (var c : cookies) {
                    cookieBuilder.append(c.getName()).append("=").append(c.getValue()).append("; ");
                }
                spec.header("Cookie", cookieBuilder.toString());
            }

            ApiResponse<java.util.List<Map<String, Object>>> res = spec.body(ids)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<java.util.List<Map<String, Object>>>>() {});

            if (res != null && res.getData() != null) {
                return res.getData().stream()
                        .map(data -> {
                            Map<String, Object> user = (Map<String, Object>) data.get("userResponse");
                            CustomerResponse c = new CustomerResponse();
                            c.setId(UUID.fromString(data.get("id").toString()));
                            if (user != null) {
                                c.setUserId(UUID.fromString(user.get("id").toString()));
                                c.setFullName((String) user.get("fullName"));
                                c.setEmail((String) user.get("email"));
                                c.setPhone((String) user.get("phone"));
                            }
                            return c;
                        })
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
