package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.CustomerResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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

    private void forwardAuthHeaders(RestClient.RequestBodySpec spec) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Forward Authorization header
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                spec.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            // Forward Cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (Cookie cookie : cookies) {
                    cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                }
                spec.header(HttpHeaders.COOKIE, cookieBuilder.toString());
            }
        }
    }

    private void forwardAuthHeaders(RestClient.RequestHeadersSpec<?> spec) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Forward Authorization header
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                spec.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            // Forward Cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (Cookie cookie : cookies) {
                    cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                }
                spec.header(HttpHeaders.COOKIE, cookieBuilder.toString());
            }
        }
    }

    public CustomerResponse getCustomerById(UUID id) {
        if (id == null)
            return null;
        Map<UUID, CustomerResponse> customers = getCustomersByIds(Collections.singletonList(id));
        return customers.get(id);
    }

    public List<UUID> searchCustomerIdsByKeyword(String keyword, UUID franchiseId) {
        try {
            // Step 1: Search Users in Identity Service
            var identitySpec = RestClient.builder().baseUrl("http://localhost:3004").build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users/search")
                            .queryParam("size", 50)
                            .queryParam("keyword", keyword)
                            .build());

            forwardAuthHeaders(identitySpec);

            var identityRes = identitySpec.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                    });
            if (identityRes != null && identityRes.getData() != null) {
                Object content = identityRes.getData().get("content");
                if (content instanceof List) {
                    List<Map<String, Object>> userList = (List<Map<String, Object>>) content;
                    List<UUID> userIds = userList.stream()
                            .map(m -> m.get("id"))
                            .filter(java.util.Objects::nonNull)
                            .map(id -> UUID.fromString(id.toString()))
                            .collect(java.util.stream.Collectors.toList());

                    if (userIds.isEmpty())
                        return Collections.emptyList();

                    // Step 2: Resolve User IDs to Customer IDs in Customer Service
                    var customerSpec = RestClient.builder().baseUrl("http://localhost:3003").build()
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/customers/search")
                                    .queryParam("franchiseId", franchiseId)
                                    .queryParam("userIds",
                                            userIds.stream().map(UUID::toString)
                                                    .collect(java.util.stream.Collectors.joining(",")))
                                    .build());

                    forwardAuthHeaders(customerSpec);

                    var customerRes = customerSpec.retrieve()
                            .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                            });
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
            // Call Identity Service (Port 3004) directly to get user names
            var searchSpec = RestClient.builder().baseUrl("http://localhost:3004").build()
                    .post()
                    .uri("/api/auth/users/bulk")
                    .body(ids);

            forwardAuthHeaders(searchSpec);

            ApiResponse<List<Map<String, Object>>> res = searchSpec.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {});

            if (res != null && res.getData() != null) {
                List<Map<String, Object>> userList = res.getData();

                return userList.stream()
                    .map(data -> {
                        CustomerResponse c = new CustomerResponse();
                        // Identity service returns User UUID in 'id'
                        if (data.get("id") != null) {
                            UUID userId = UUID.fromString(data.get("id").toString());
                            c.setUserId(userId);
                            c.setId(userId); // Also set as ID for safety
                        }
                        c.setFullName((String) data.get("fullName"));
                        c.setEmail((String) data.get("email"));
                        c.setPhone((String) data.get("phone"));
                        return c;
                    })
                    .filter(c -> c.getUserId() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            CustomerResponse::getUserId, 
                            c -> c, 
                            (c1, c2) -> c1));
            }
        } catch (Exception e) {
            log.error("Error bulk fetching users from Identity Service for IDs {}: {}", ids, e.getMessage());
        }
        return Collections.emptyMap();
    }

    public void saveCustomerFranchise(UUID customerId, UUID franchiseId) {
        if (customerId == null || franchiseId == null)
            return;
        try {
            var spec = RestClient.builder().baseUrl("http://localhost:3003").build()
                    .post()
                    .uri("/api/customers/save-customer-franchise");

            forwardAuthHeaders(spec);

            Map<String, Object> requestBody = Map.of(
                    "customerId", customerId,
                    "franchiseId", franchiseId);

            spec.body(requestBody).retrieve().toBodilessEntity();
            log.info("Successfully saved CustomerFranchise link for customerId {} and franchiseId {}", customerId,
                    franchiseId);
        } catch (Exception e) {
            log.error("Error saving CustomerFranchise for customerId {}: {}", customerId, e.getMessage());
        }
    }
}
