package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.OrderResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.StatusTransaction;

import java.util.UUID;

public interface PaymentTransactionService {
    void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode);
    PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId);
    OrderResponse checkDuplicateTransaction(OrderResponse orderResponse);
}
