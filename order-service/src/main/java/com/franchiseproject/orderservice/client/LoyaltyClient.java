package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.LoyaltyDeductRequest;
import com.franchiseproject.orderservice.dto.request.LoyaltyEarnRequest;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoyaltyClient {
    private final RestClient apiLoyaltyRestClient;

    // 1. DEDUCT POINTS WHEN CUSTOMER USES THEM
    public void apiLoyaltyDeduct(UUID customerId, UUID franchiseId, UUID orderId, Integer pointsToDeduct) {
        try {
            LoyaltyDeductRequest request = new LoyaltyDeductRequest(customerId, franchiseId, orderId, pointsToDeduct);

            // Call deduct API in Loyalty service
            apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/deduct")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity(); // Only need to ensure success (200 OK)

        } catch (HttpClientErrorException e) {
            log.warn("Loyalty 4xx error: {}", e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) { // Code 400 or 404 depending on ErrorCode configuration
                throw new AppException(ErrorCode.INVALID_LOYALTY);
            }
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        } catch (RestClientException e) {
            log.error("Unknown RestClient error", e);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    // 2. EARN POINTS ON SUCCESSFUL ORDER
    public void apiLoyaltyEarn(UUID customerId, UUID franchiseId, UUID orderId, Double orderAmount) {
        try {
            // Loyalty needs orderAmount to calculate points
            LoyaltyEarnRequest request = new LoyaltyEarnRequest(customerId, franchiseId, orderId, orderAmount);

            apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/earn")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully earned points for order: {}", orderId);
        } catch (Exception e) {
            // Earning points failure should not fail the order, log for admin to resolve
            log.error("Failed to earn Loyalty points for order {}: ", orderId, e);
        }
    }

    // 3. REFUND POINTS FOR FAILED/CANCELED ORDER
//    public void apiLoyaltyTraceBackPoints(UUID customerId, UUID franchiseId, UUID orderId, Integer pointsToRefund) {
//        try {
//            LoyaltyTraceBackRequest request = new LoyaltyTraceBackRequest(customerId, franchiseId, orderId, pointsToRefund);
//
//            apiLoyaltyRestClient.post()
//                    .uri("/api/loyalty/refund")
//                    .body(request)
//                    .retrieve()
//                    .toBodilessEntity();
//            log.info("Successfully refunded {} points for order {}", pointsToRefund, orderId);
//        } catch (Exception e) {
//            // Traceback failure should only be logged (or pushed to Kafka for later retry)
//            log.error("Failed to refund points for order {}: ", orderId, e);
//        }
//    }

}
