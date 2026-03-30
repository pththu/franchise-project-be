package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.EarnPointsRequest;
import com.franchiseproject.orderservice.dto.request.LoyaltyReserveRequest;
import com.franchiseproject.orderservice.dto.request.LoyaltyTraceBackRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.EarnPointsResponse;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoyaltyClient {
    private final RestClient apiLoyaltyRestClient;

    /// Method check điểm Loyalty để trừ hóa đơn cho order và giữ điểm
    public BigDecimal apiLoyaltyReserve(UUID customerId, UUID franchiseId, UUID orderId, Integer loyaltyPoints) {
        try {
            LoyaltyReserveRequest request = new LoyaltyReserveRequest(customerId, franchiseId, orderId, loyaltyPoints);
            ApiResponse<EarnPointsResponse> response = apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/deduct")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<EarnPointsResponse>>() {});

            if (response != null && response.getData() != null) {
                // Return discount value: points * 100
                int pointsDeducted = Math.abs(response.getData().getPointsEarned());
                return BigDecimal.valueOf(pointsDeducted).multiply(BigDecimal.valueOf(100));
            }
            return BigDecimal.ZERO;
        } catch (HttpClientErrorException e) {
            log.warn("Loyalty 4xx error: {}", e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) {
                throw new AppException(ErrorCode.INVALID_LOYALTY);
            }
            if (e.getStatusCode().value() == 404) {
                throw new AppException(ErrorCode.LOYALTY_NOT_FOUND);
            }
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Loyalty service error → fallback no discount", e);
            return BigDecimal.ZERO;
        } catch (RestClientException e) {
            log.error("Unknown RestClient error", e);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public void apiLoyaltyEarn(UUID userId, UUID franchiseId, Double orderAmount) {
        try {
            EarnPointsRequest request = new EarnPointsRequest(userId, franchiseId, orderAmount);
            apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/earn")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Loyalty earn points request sent for user {}", userId);
        } catch (Exception e) {
            log.error("Loyalty earn points failed", e);
        }
    }

    /// Method traceback điểm Loyalty sau khi order failed
    public void apiLoyaltyTraceBackPoints(UUID customerId, UUID franchiseId, UUID orderId, Integer pointsToRefund) {
        try {
            LoyaltyTraceBackRequest request = new LoyaltyTraceBackRequest(customerId, franchiseId, orderId, pointsToRefund);
            apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/refund")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Loyalty trace back points request received");
        } catch (Exception e) {
            log.error("Loyalty traceback failed", e);
        }
    }
}
