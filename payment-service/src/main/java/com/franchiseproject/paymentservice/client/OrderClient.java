package com.franchiseproject.paymentservice.client;

import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderClient {

    private final RestClient restClient;

    // yêu cầu order-service get info order sau đó trả lại theo OrderResponse
    public OrderResponse getOrderInfoByOrderId(UUID orderId) {
        try {
            ApiResponse<OrderResponse> response = restClient.get()
                    .uri("/api/orders/detail/{orderId}", orderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return response.getData();
        } catch (Exception e) {
            throw new AppException(ErrorCode.NOT_FOUND_ORDER);
        }
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
