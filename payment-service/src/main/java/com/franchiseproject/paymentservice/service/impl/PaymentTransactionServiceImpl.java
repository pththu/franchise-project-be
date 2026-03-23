package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.OrderClient;
import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.MomoResultCode;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import com.franchiseproject.paymentservice.mapper.PaymentTransactionMapper;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    PaymentTransactionRepository paymentTransactionRepository;
    OrderClient orderClient;
    PaymentTransactionMapper paymentTransactionMapper;

    ///  Dùng build Payment Transaction
    @Override
    @Transactional
    public PaymentTransaction buildPaymentTransaction(OrderResponse orderResponse, PaymentMethod paymentMethod) {
        return PaymentTransaction.builder()
                .orderId(orderResponse.getId())
                .amount(orderResponse.getTotalDue())
                .status(StatusTransaction.CREATED)
                .paymentMethod(paymentMethod)
                .transactionRef(null)
                .build();
    }

    ///  Dùng để set một số field và gửi kết quả giao dịch cho order-service sau khi Momo gửi inp
    @Override
    @Transactional
    public void handlePaymentTransaction(Long transId, UUID paymentTransactionId, Integer resultCode) {
        log.info("IPN start handle transaction: {}", paymentTransactionId);
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_TRANSACTION));
        paymentTransaction.setTransactionRef(transId.toString());
        log.info("Status before: {}", paymentTransaction.getStatus());
        log.info("Status after: {}", paymentTransaction.getStatus());
        setStatusTransaction(paymentTransaction, resultCode);
        paymentTransactionRepository.save(paymentTransaction);
        PaymentResultRequest paymentResultRequest = PaymentResultRequest.builder()
                .paymentTransactionId(paymentTransaction.getId())
                .orderId(paymentTransaction.getOrderId())
                .amount(paymentTransaction.getAmount())
                .paymentMethod(paymentTransaction.getPaymentMethod().getMethodName())
                .transactionRef(paymentTransaction.getTransactionRef())
                .status(paymentTransaction.getStatus())
                .build();
        orderClient.sendPaymentResult(paymentResultRequest);
    }

    /// Lấy giao dịch trong DB theo orderId
    @Override
    public PaymentTransactionResponse getPaymentTransactionByOrderId(UUID orderId) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_TRANSACTION));
        return paymentTransactionMapper.toPaymentTransactionResponse(transaction);
    }

    /// Kiểm tra order xem có ở trạng thái yêu cầu giao dịch không
    /// Kiểm tra order xem đã có tạo giao dịch từ trước chưa(tránh duplicate giao dịch)
    @Override
    @Transactional
    public void checkDuplicateTransaction(OrderResponse orderResponse) {
        if (!orderResponse.getOrderStatus().equals("WAITING_PAYMENT")) {
            throw new AppException(ErrorCode.ORDER_NOT_PAYABLE);
        }
        boolean exists = paymentTransactionRepository
                .findByOrderId(orderResponse.getId())
                .isPresent();
        if (exists) {
            throw new AppException(ErrorCode.DUPLICATE_ORDER_ID);
        }
    }

    /// Set lại trạng thái giao dịch sau khi Momo gửi resultCode
    private PaymentTransaction setStatusTransaction(PaymentTransaction transaction, Integer resultCode) {
        MomoResultCode momoCode = MomoResultCode.fromCode(resultCode);
        if (momoCode == null) {
            transaction.changeStatus(StatusTransaction.FAILED);
            return transaction;
        }
        return switch (momoCode) {
            ///  Result Code Success
            case SUCCESS -> {
                transaction.changeStatus(StatusTransaction.SUCCESS);
                yield transaction;
            }
            /// Result Code Cancel
            case USER_CANCELLED, PARTNER_CANCELLED -> {
                transaction.changeStatus(StatusTransaction.CANCELLED);
                yield transaction;
            }
            /// Result Code Expire
            case EXPIRED -> {
                transaction.changeStatus(StatusTransaction.EXPIRED);
                yield transaction;
            }
            /// ResultCode FAIL
            case FAILED_BALANCE, FAILED_REJECTED, FAILED_LIMIT, FAILED_USER_DENY, FAILED_ACCOUNT -> {
                transaction.changeStatus(StatusTransaction.FAILED);
                yield transaction;
            }
            ///  Result Code Processing
            case PENDING_CONFIRM, PROCESSING, PROVIDER_PROCESSING ->
                // giữ nguyên PENDING
                    transaction;
            default -> {
                transaction.changeStatus(StatusTransaction.FAILED);
                yield transaction;
            }
        };
    }

    @Override
    @Scheduled(fixedRate = 60000) // chạy mỗi 60s
    @Transactional /// cần update thêm
    public void expirePendingTransactions() {
        Instant timeout = Instant.now().minus(15, ChronoUnit.MINUTES);
        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findExpiredTransactions(timeout);
        for (PaymentTransaction tx : transactions) {
            try {
                expireTransaction(tx);
                log.info("Expired transaction: {}", tx.getOrderId());
            } catch (Exception e) {
                log.error("Error expiring transaction {}", tx.getOrderId(), e);
            }
        }
    }

    @Override
    public PaymentTransaction createPaymentTransaction(PaymentTransaction paymentTransaction) {
        return paymentTransactionRepository.save(paymentTransaction);
    }

    @Transactional
    public void expireTransaction(PaymentTransaction tx) {
        if (tx.getStatus() != StatusTransaction.PENDING) {
            return;
        }
        tx.setStatus(StatusTransaction.EXPIRED);
        paymentTransactionRepository.save(tx);
        // notify order service
        orderClient.sendPaymentResult(
                PaymentResultRequest.builder()
                        .orderId(tx.getOrderId())
                        .paymentTransactionId(tx.getId())
                        .transactionRef(tx.getTransactionRef())
                        .amount(tx.getAmount())
                        .paymentMethod(tx.getPaymentMethod().getMethodName())
                        .status(tx.getStatus())
                        .build()
        );
    }

}
