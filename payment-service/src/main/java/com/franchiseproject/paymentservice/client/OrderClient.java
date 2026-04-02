package com.franchiseproject.paymentservice.client;

import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderClient {

    private final RestClient restClient;

    // yêu cầu order-service get info order sau đó trả lại theo OrderResponse
    public OrderResponse getOrderInfoByOrderId(UUID orderId) {
        int maxRetries = 3;
        int delayMs = 500;
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                ApiResponse<OrderResponse> response = restClient.get()
                        .uri("/api/orders/detail/{orderId}", orderId)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });
                
                if (response != null && response.getData() != null) {
                    return response.getData();
                }
                
                log.warn("Order info response data is null for order {}. Retry {}/{}", orderId, i + 1, maxRetries);
            } catch (Exception e) {
                lastException = e;
                log.warn("Failed to fetch order info for {}: {}. Retry {}/{}", orderId, e.getMessage(), i + 1, maxRetries);
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.error("Final attempt to fetch order info for {} failed.", orderId, lastException);
        throw new AppException(ErrorCode.NOT_FOUND_ORDER);
    }


    // Yêu cầu cập nhật trạng thái đơn hàng
    public void updateOrderStatus(UUID orderId, String status) {
        try {
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/{orderId}/status")
                            .queryParam("status", status)
                            .build(orderId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UPDATE_ORDER_STATUS_FAILED);
        }
    }

    /// Gửi kết quả giao dịch cho order set lại trạng thái
    public void sendPaymentResult(PaymentResultRequest request) {

        restClient.post()
                .uri("/api/orders/payment-result")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
