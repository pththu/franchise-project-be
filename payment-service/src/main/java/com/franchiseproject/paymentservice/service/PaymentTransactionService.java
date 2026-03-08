package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentTransactionService {
    List<PaymentTransactionResponse> getTransactionsByUserId(UUID userId);
}
