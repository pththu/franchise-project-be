package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.InventoryReserveRequest;
import com.franchiseproject.orderservice.dto.request.InventorySubtractRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class InventoryClient {
    private final RestClient inventoryRestClient = RestClient.builder()
            .baseUrl("http://localhost:3002")
            .defaultHeader("Content-Type", "application/json")
            .build();

    public void reserveStock(InventoryReserveRequest request) {
        try {
            inventoryRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory/stocks/reserve")
                            .queryParam("locationId", request.getLocationId())
                            .build())
                    .body(request.getItems())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Reserve stock success for location: {}", request.getLocationId());
        } catch (HttpClientErrorException e) {
            log.warn("Reserve stock 4xx error: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Đặt giữ kho thất bại: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Inventory service down", e);
            throw new RuntimeException("Lỗi kết nối inventory-service");
        }
    }

    public void releaseStock(InventoryReserveRequest request) {
        try {
            inventoryRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory/stocks/release")
                            .queryParam("locationId", request.getLocationId())
                            .build())
                    .body(request.getItems())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Release stock success for location: {}", request.getLocationId());
        } catch (HttpClientErrorException e) {
            log.warn("Release stock 4xx error: {}", e.getResponseBodyAsString());
            // Don't throw exception on release failure to avoid blocking rollbacks
        } catch (Exception e) {
            log.error("Release stock failed", e);
        }
    }

    public void commitStock(InventoryReserveRequest request) {
        try {
            inventoryRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory/stocks/commit")
                            .queryParam("locationId", request.getLocationId())
                            .build())
                    .body(request.getItems())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Commit stock success for location: {}", request.getLocationId());
        } catch (HttpClientErrorException e) {
            log.warn("Commit stock 4xx error: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Khấu trừ kho thất bại: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Commit service down", e);
            throw new RuntimeException("Lỗi kết nối inventory-service");
        }
    }

    public void notifyNewOrder(java.util.UUID franchiseId) {
        try {
            inventoryRestClient.post()
                    .uri("/api/inventory/notify/new-order/" + franchiseId)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Notify new order success for franchise: {}", franchiseId);
        } catch (Exception e) {
            log.warn("Notify new order failed for franchise: {}", franchiseId, e);
        }
    }

    public void notifyOrderStatus(java.util.UUID orderId, String status, java.util.UUID franchiseId) {
        try {
            inventoryRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory/notify/order-status/" + orderId)
                            .queryParam("status", status)
                            .queryParam("franchiseId", franchiseId)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Notify order status update success for order: {}", orderId);
        } catch (Exception e) {
            log.warn("Notify order status update failed for order: {}", orderId, e);
        }
    }

    public java.util.Map<java.util.UUID, Integer> getBulkAvailableStock(java.util.List<java.util.UUID> variantIds) {
        try {
            var response = inventoryRestClient.post()
                    .uri("/api/inventory/stocks/bulk-available")
                    .body(variantIds)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<com.franchiseproject.orderservice.dto.response.ApiResponse<java.util.Map<java.util.UUID, Integer>>>() {});
            
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch bulk available stock", e);
        }
        return java.util.Map.of();
    }
}
