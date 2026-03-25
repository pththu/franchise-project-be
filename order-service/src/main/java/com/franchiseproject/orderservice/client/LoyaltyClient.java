package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.LoyaltyReserveRequest;
import com.franchiseproject.orderservice.dto.request.LoyaltyTraceBackRequest;
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

    /// Method check điểm Loyalty để trừ hóa đơn cho order và giữ điểm
    public BigDecimal apiLoyaltyReserve(UUID customerId, Integer loyaltyPoints) {
        try {
            LoyaltyReserveRequest request = new LoyaltyReserveRequest(customerId, loyaltyPoints);
            return apiLoyaltyRestClient.post()
                    .uri("/api/loyalty/reserve")
                    .body(request)
                    .retrieve()
                    .body(BigDecimal.class);
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
