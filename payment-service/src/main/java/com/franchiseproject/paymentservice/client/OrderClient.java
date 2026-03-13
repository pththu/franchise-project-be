package com.franchiseproject.paymentservice.client;

import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.dto.response.OrderResponse;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderClient {

    private final RestClient orderRestClient;

    //yêu cầu order-service get info order sau đó trả lại theo OrderResponse
    public OrderResponse getOrderInfoByOrderId(UUID orderId) {
        try {
            return orderRestClient.get()
                    .uri("/api/orders/{orderId}/get-orders", orderId)
                    .retrieve()
                    .body(OrderResponse.class);
        } catch (AppException a) {
            throw new AppException(ErrorCode.NOT_FOUND_ORDER);
        }
    }

    public void sendPaymentResult(PaymentResultRequest request) {

        orderRestClient.post()
                .uri("/api/orders/payment-result")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
