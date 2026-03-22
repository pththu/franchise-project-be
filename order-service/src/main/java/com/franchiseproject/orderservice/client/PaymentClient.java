package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.PaymentTransactionRequest;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentClient {
    private final RestClient paymentRestClient;

    public PaymentQRResponse createTransaction(UUID orderId, UUID paymentMethodId) {
        try {
            PaymentTransactionRequest request = new PaymentTransactionRequest(orderId, paymentMethodId);
            paymentRestClient.post()
                    .uri("/api/payments/init")
                    .body(request)
                    .retrieve()
                    .body(PaymentQRResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("Payment 4xx error: {}", e.getResponseBodyAsString());
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Payment service down", e);
            throw new AppException(ErrorCode.PAYMENT_INIT_FAILED);
        }
    }
    //Mock Test
//    public PaymentResponse createTransaction(UUID orderId,
//                                             UUID customerId,
//                                             BigDecimal finalTotal) {
//
//        if (orderId == null || customerId == null || finalTotal == null) {
//            throw new AppException(ErrorCode.NO_TRANSACTION);
//        }
//
//        // MOCK RESPONSE
//        return PaymentResponse.builder()
//                .paymentTransactionId(UUID.randomUUID())
//                .orderId(orderId)
//                .customerId(customerId)
//                .finalTotal(finalTotal)
//                .orderStatus(OrderStatus.CONFIRMED)
//                .build();
//    }
}
