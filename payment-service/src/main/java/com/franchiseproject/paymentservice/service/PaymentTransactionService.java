package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;

import java.util.UUID;

public interface PaymentTransactionService {
    PaymentTransaction buildPaymentTransaction(OrderResponse orderResponse, PaymentMethod paymentMethod);

    void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode);

    PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId);

    void checkDuplicateTransaction(OrderResponse orderResponse);

    void expirePendingTransactions();

    void createPaymentTransaction(PaymentTransaction paymentTransaction);
}
