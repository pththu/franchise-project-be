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
                    .uri("/api/inventory/stocks/reserve")
                    .body(request)
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

    public void subtractStock(InventorySubtractRequest request) {
        try {
            inventoryRestClient.post()
                    .uri("/api/inventory/stocks/subtract")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Subtract stock success for location: {}", request.getLocationId());
        } catch (HttpClientErrorException e) {
            log.warn("Subtract stock 4xx error: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Trừ kho thất bại: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Inventory service down", e);
            throw new RuntimeException("Lỗi kết nối inventory-service");
        }
    }
}
