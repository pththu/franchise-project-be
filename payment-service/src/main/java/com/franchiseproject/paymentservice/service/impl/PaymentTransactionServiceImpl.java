package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.mapper.PaymentTransactionMapper;
import com.franchiseproject.paymentservice.repository.PaymenMethodRepository;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentTransactionServiceImpl implements PaymentTransactionService {
    PaymentTransactionRepository paymentTransactionRepository;4
    PaymentTransactionMapper paymentTransactionMapper;


    @Override
    public List<PaymentTransactionResponse> getTransactionsByUserId(UUID userId) {
        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(paymentTransactionMapper::toPaymentTransactionResponse)
                .collect(Collectors.toList());
    }
}
