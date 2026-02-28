package com.franchiseproject.orderservice.infrastructure.client;

import com.franchiseproject.orderservice.dto.request.PaymentTransactionRequest;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentClient {
    private final RestClient paymentRestClient;

    public PaymentResponse createTransaction(UUID orderId, UUID customerId,BigDecimal finalTotal) {
        PaymentTransactionRequest request = new PaymentTransactionRequest(orderId, customerId,finalTotal);
        PaymentResponse paymentResponse = paymentRestClient.post()
                .uri("/api/payments/validate")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
        return Optional.ofNullable(paymentResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NO_TRANSACTION));
    }
}
