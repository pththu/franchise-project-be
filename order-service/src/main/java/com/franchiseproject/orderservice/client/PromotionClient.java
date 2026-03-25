package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.PromotionReserveRequest;
import com.franchiseproject.orderservice.dto.request.PromotionTraceBackRequest;
import com.franchiseproject.orderservice.dto.response.PromotionDiscountResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
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
public class PromotionClient {
    private final RestClient apiPromotionRestClient;

    /// Method check promotion cho order và giữ lượt promotion
    public PromotionDiscountResponse apiPromotionReserve(UUID promotionId, UUID franchiseId, UUID customerId, UUID orderId, BigDecimal totalItems) {
        try {
            PromotionReserveRequest request = new PromotionReserveRequest(franchiseId, customerId, promotionId, orderId, totalItems);
            return apiPromotionRestClient.post()
                    .uri("/api/promotions/reserve")
                    .body(request)
                    .retrieve()
                    .body(PromotionDiscountResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("Promotion 4xx error: {}", e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) {
                throw new AppException(ErrorCode.INVALID_PROMOTION);
            }
            if (e.getStatusCode().value() == 404) {
                throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
            }
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Promotion service error → fallback no discount", e);
            return PromotionDiscountResponse.builder()
                    .promotionUsageId(null)
                    .discountValue(BigDecimal.ZERO)
                    .discountType(null)
                    .build();
        } catch (RestClientException e) {
            log.error("Unknown RestClient error", e);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /// Method traceback cho promotion khi order bị failed
    public void apiPromotionTraceBack(UUID orderId, OrderStatus orderStatus) {
        try {
            PromotionTraceBackRequest request = new PromotionTraceBackRequest(orderId, orderStatus);
            apiPromotionRestClient.post()
                    .uri("/api/promotions/trace")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Promotion rollback success: customerId={}", orderId);
        } catch (Exception e) {
            log.error("Promotion traceback failed", e);
        }
    }


    /// Mocktest
    //    public BigDecimal validateAndCalculate(UUID customerId,
//                                           UUID promotionId,
//                                           BigDecimal totalItems) {
//        if (promotionId == null) {
//            return BigDecimal.ZERO;
//        }
//        // Giả lập giảm 10%
//        return totalItems.multiply(BigDecimal.valueOf(0.1));
//    }
}
