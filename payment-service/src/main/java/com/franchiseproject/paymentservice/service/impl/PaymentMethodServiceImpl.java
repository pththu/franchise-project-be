package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.OrderClient;
import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.request.CreatePaymentRequest;
import com.franchiseproject.paymentservice.dto.request.PaymentResultRequest;
import com.franchiseproject.paymentservice.dto.response.CreatePaymentResponse;
import com.franchiseproject.paymentservice.service.VnpayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;
import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentMethodResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import com.franchiseproject.paymentservice.mapper.PaymentMethodMapper;
import com.franchiseproject.paymentservice.repository.PaymentMethodRepository;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.MomoService;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    PaymentMethodRepository paymentMethodRepository;
    PaymentTransactionService paymentTransactionService;
    PaymentMethodMapper paymentMethodMapper;
    PaymentTransactionRepository paymentTransactionRepository;
    OrderClient orderClient;
    MomoService momoService;
    VnpayService vnpayService;

    @Override
    public List<PaymentMethod> getAll() {
        return paymentMethodRepository.findAll();
    }

    /// Lấy tất cả các phương thức thanh toán khả dụng
    @Override
    @Transactional
    public List<PaymentMethodResponse> getAllByActiveTrue() {
        List<PaymentMethod> listPaymentMethod = paymentMethodRepository.findAllByActive(true)
                .orElseThrow(() -> new AppException(ErrorCode.METHOD_EMPTY));
        return paymentMethodMapper.toPaymentMethodResponse(listPaymentMethod);

    }

    @Override
    public PaymentMethod create(PaymentMethod paymentMethod) {
        return paymentMethodRepository.save(paymentMethod);
    }

    /// Lấy phương thức thanh toán mà user đã chọn ở FE
    @Override
    @Transactional
    public PaymentMethod getAvailiablePaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest) {
        return paymentMethodRepository
                .findByIdAndActiveTrue(optionPaymentMethodRequest.getPaymentMethodId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.PAYMENT_METHOD_NOT_AVAILABLE));
    }

    /// Điều hướng phương thức tạo giao dịch theo lựa chọn User đã chọn ở FE
    @Override
    @Transactional
    public PaymentQRResponse optionPaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest) {
        try {
        log.info("Initializing payment transaction for order: " + optionPaymentMethodRequest.getOrderId());
        OrderResponse orderResponse = orderClient.getOrderInfoByOrderId(optionPaymentMethodRequest.getOrderId());
        log.info("Fetched order for payment: " + orderResponse);
        
        PaymentMethod paymentMethod = getAvailiablePaymentMethod(optionPaymentMethodRequest);
        log.info("Fetched payment method: " + paymentMethod.getMethodName());
        
        paymentTransactionService.checkDuplicateTransaction(orderResponse);
        log.info("Duplicate check passed");

        switch (paymentMethod.getMethodName()) {
            case "MOMO":
                CreateMomoResponse createMomoResponse = momoService.buildCreateMomoQR(orderResponse, paymentMethod);
                log.info("Create Momo QR Response:" + createMomoResponse.getPayUrl());
                return PaymentQRResponse.builder()
                        .paymentTransactionId(UUID.fromString(createMomoResponse.getRequestId()))
                        .method("MOMO")
                        .paymentUrl(createMomoResponse.getPayUrl())
                        .qrCodeUrl(createMomoResponse.getQrCodeUrl())
                        .amount(createMomoResponse.getAmount())
                        .build();
            case "VNPAY":
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                    HttpServletRequest httpRequest = attributes.getRequest();
                    
                    // NEW: Create PENDING transaction for VNPay to allow expiration cleanup
                    PaymentTransaction p = paymentTransactionService.buildPaymentTransaction(orderResponse, paymentMethod);
                    p.changeStatus(StatusTransaction.PENDING);
                    PaymentTransaction savedTx = paymentTransactionRepository.save(p);

                    CreatePaymentRequest vnpayReq = CreatePaymentRequest.builder()
                            .amount(orderResponse.getTotalDue().longValue())
                            .orderId(orderResponse.getId())
                            .build();

                    CreatePaymentResponse vnpayRes = vnpayService.createPaymentUrl(vnpayReq, httpRequest, savedTx.getId());

                    return PaymentQRResponse.builder()
                            .paymentTransactionId(savedTx.getId()) // Use the saved ID
                            .method("VNPAY")
                            .paymentUrl(vnpayRes.getPaymentUrl())
                            .amount(orderResponse.getTotalDue().longValue())
                            .build();
                } catch (Exception e) {
                    throw new RuntimeException("VNPAY URL generation failed", e);
                }
            case "COD":
                try {
                UUID txId = null;
                String typeOrder = orderResponse.getTypeOrder() != null ? orderResponse.getTypeOrder().trim() : "";
                
                if ("POS".equalsIgnoreCase(typeOrder)) {
                    PaymentTransaction p = PaymentTransaction.builder()
                            .amount(orderResponse.getTotalDue())
                            .orderId(orderResponse.getId())
                            .status(StatusTransaction.SUCCESS)
                            .paymentMethod(paymentMethod)
                            .build();
                    PaymentTransaction saved = paymentTransactionService.createPaymentTransaction(p);
                    txId = saved.getId();

                } else if ("Online".equalsIgnoreCase(typeOrder)) {
                    PaymentTransaction p = PaymentTransaction.builder()
                            .amount(orderResponse.getTotalDue())
                            .orderId(orderResponse.getId())
                            .status(StatusTransaction.PENDING)
                            .paymentMethod(paymentMethod)
                            .build();
                    PaymentTransaction saved = paymentTransactionService.createPaymentTransaction(p);
                    txId = saved.getId();
                }
                return PaymentQRResponse.builder()
                        .amount(orderResponse.getTotalDue().longValue())
                        .method("COD")
                        .paymentTransactionId(txId)
                        .build();
                } catch (Exception e) {
                    log.error("COD payment creation failed", e);
                    throw e;
                }
                
            default:
                throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        } catch (Exception e) {
            log.error("optionPaymentMethod failed for order: " + optionPaymentMethodRequest.getOrderId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public String handleVnpayCallback(Map<String, String> params) {
        try {
            if (vnpayService.validateReturnData(params)) {
                String txnRef = params.get("vnp_TxnRef");
                String[] parts = txnRef.split("_");
                if (parts.length < 2) return null;
                
                String orderIdStr = parts[0];
                UUID orderId = UUID.fromString(orderIdStr);
                
                // Fetch existing transaction to check for idempotency
                PaymentTransaction existingTx = paymentTransactionRepository.findByOrderId(orderId).orElse(null);
                if (existingTx != null && (existingTx.getStatus() == StatusTransaction.SUCCESS || existingTx.getStatus() == StatusTransaction.FAILED)) {
                    log.info("VNPay callback already processed for order {}. Skipping.", orderId);
                    String typeOrder = "POS";
                    try {
                        com.franchiseproject.paymentservice.dto.response.order.OrderResponse order = orderClient.getOrderInfoByOrderId(orderId);
                        if (order != null) typeOrder = order.getTypeOrder();
                    } catch (Exception e) {
                        log.info("Order info not found for {} (processed elsewhere).", orderId);
                    }
                    return orderIdStr + "|" + typeOrder + "|" + params.get("vnp_ResponseCode");
                }

                com.franchiseproject.paymentservice.dto.response.order.OrderResponse order = null;
                try {
                    order = orderClient.getOrderInfoByOrderId(orderId);
                } catch (Exception e) {
                    log.info("Order info not found for {} during VNPay callback processing.", orderId);
                }
                
                String typeOrder = (order != null) ? order.getTypeOrder() : "POS";
                String responseCode = params.get("vnp_ResponseCode");
                final com.franchiseproject.paymentservice.dto.response.order.OrderResponse finalOrder = order;

                if ("00".equals(responseCode)) {
                    BigDecimal amount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100));

                    PaymentMethod method = paymentMethodRepository.findByMethodName("VNPAY")
                            .orElseThrow(() -> new RuntimeException("Payment method VNPAY not found"));

                    // Find existing transaction or create if missing (protective fallback)
                    PaymentTransaction p = paymentTransactionRepository.findByOrderId(UUID.fromString(orderIdStr))
                            .orElseGet(() -> paymentTransactionService.buildPaymentTransaction(finalOrder, method));
                    
                    p.setAmount(amount);
                    p.setStatus(StatusTransaction.SUCCESS);
                    p.setTransactionRef(params.get("vnp_TransactionNo")); // Use actual VNP transaction no
                    PaymentTransaction savedTx = paymentTransactionRepository.save(p);

                    if (order != null) {
                        orderClient.sendPaymentResult(com.franchiseproject.paymentservice.dto.request.PaymentResultRequest.builder()
                                .orderId(UUID.fromString(orderIdStr))
                                .paymentTransactionId(savedTx.getId())
                                .amount(amount)
                                .paymentMethod("VNPAY")
                                .status(StatusTransaction.SUCCESS)
                                .build());
                    }
                } else {
                    BigDecimal amount = params.get("vnp_Amount") != null 
                        ? new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100))
                        : BigDecimal.ZERO;

                    PaymentMethod method = paymentMethodRepository.findByMethodName("VNPAY")
                            .orElseThrow(() -> new RuntimeException("Payment method VNPAY not found"));

                    PaymentTransaction p = paymentTransactionRepository.findByOrderId(UUID.fromString(orderIdStr))
                            .orElseGet(() -> paymentTransactionService.buildPaymentTransaction(finalOrder, method));
                    
                    p.setAmount(amount);
                    p.setStatus(StatusTransaction.FAILED);
                    p.setTransactionRef(params.get("vnp_TransactionNo"));
                    PaymentTransaction savedTx = paymentTransactionRepository.save(p);

                    if (order != null) {
                        orderClient.sendPaymentResult(com.franchiseproject.paymentservice.dto.request.PaymentResultRequest.builder()
                                .orderId(UUID.fromString(orderIdStr))
                                .paymentTransactionId(savedTx.getId())
                                .amount(amount)
                                .paymentMethod("VNPAY")
                                .status(StatusTransaction.FAILED)
                                .build());
                    }
                }

                return orderIdStr + "|" + typeOrder + "|" + responseCode;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("VNPAY Callback processing failed", e);
        }
    }
}
