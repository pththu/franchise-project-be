package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.OrderClient;
import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentTransactionImpl implements PaymentTransactionService {
    PaymentTransactionRepository paymentTransactionRepository;
    OrderClient orderClient;

    @Override
    @Transactional
    public void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.getById(paymentTransactionId);
        paymentTransaction.setTransactionRef(transId.toString());
        if (resultCode == 0) {
            paymentTransaction.setStatus(StatusTransaction.SUCCESS);
        } else {
            paymentTransaction.setStatus(StatusTransaction.FAILED);
        }
        PaymentResultRequest paymentResultRequest = PaymentResultRequest.builder()
                .paymentTransactionId(paymentTransaction.getId())
                .orderId(paymentTransaction.getOrderId())
                .amount(paymentTransaction.getAmount())
                .paymentMethod(paymentTransaction.getPaymentMethod().getMethodName())
                .transactionRef(paymentTransaction.getTransactionRef())
                .status(paymentTransaction.getStatus().toString())
                .build();
        paymentTransactionRepository.save(paymentTransaction);
        orderClient.sendPaymentResult(paymentResultRequest);
    }
}
