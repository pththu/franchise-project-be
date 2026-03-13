package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.OrderClient;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.mapper.PaymentTransactionMapper;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import jakarta.transaction.Transactional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentTransactionServiceImpl implements PaymentTransactionService {
    PaymentTransactionRepository paymentTransactionRepository;
    PaymentTransactionMapper paymentTransactionMapper;
    OrderClient orderClient;


    @Override
    public List<PaymentTransactionResponse> getTransactionsByUserId(UUID userId) {
        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(paymentTransactionMapper::toPaymentTransactionResponse)
                .collect(Collectors.toList());
    }

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

    @Override
    public PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_TRANSACTION));
        return paymentTransactionMapper.toPaymentTransactionResponse(transaction);
    }

}
