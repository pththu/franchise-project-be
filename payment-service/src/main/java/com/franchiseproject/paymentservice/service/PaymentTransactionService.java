package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.OrderResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.StatusTransaction;

import java.util.UUID;

public interface PaymentTransactionService {
    PaymentTransaction buildPaymentTransaction(OrderResponse orderResponse, PaymentMethod paymentMethod);

    void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode);

    PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId);

    OrderResponse checkDuplicateTransaction(OrderResponse orderResponse);

    void expirePendingTransactions();
}
