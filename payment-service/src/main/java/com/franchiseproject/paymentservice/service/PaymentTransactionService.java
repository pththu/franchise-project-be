package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import java.util.List;
import java.util.UUID;

public interface PaymentTransactionService {
    void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode);
    PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId);
    List<PaymentTransactionResponse> getTransactionsByUserId(UUID userId);
}
