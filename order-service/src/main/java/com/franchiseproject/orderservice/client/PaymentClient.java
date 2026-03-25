package com.franchiseproject.orderservice.client;

import com.franchiseproject.orderservice.dto.request.PaymentTransactionRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
    private final RestClient apiPaymentRestClient;

    public PaymentQRResponse createTransaction(UUID orderId, UUID paymentMethodId) {
        try {
            PaymentTransactionRequest request = new PaymentTransactionRequest(orderId, paymentMethodId);
            ApiResponse<PaymentQRResponse> response = apiPaymentRestClient.post()
                    .uri("/api/payments/init")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<PaymentQRResponse>>() {
                    });
            log.info("Payment request: {}", request);
            log.info("Payment response raw data: {}", response != null ? response.getData() : "null");
            if (response != null && response.getData() != null) {
                log.info("Deserialized paymentTransactionId inside Client: {}", response.getData().getPaymentTransactionId());
            }
            return response != null ? response.getData() : null;
        } catch (HttpClientErrorException e) {
            log.warn("Payment 4xx error: {}", e.getResponseBodyAsString());
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Payment service down", e);
            throw new AppException(ErrorCode.PAYMENT_INIT_FAILED);
        }
    }
}
