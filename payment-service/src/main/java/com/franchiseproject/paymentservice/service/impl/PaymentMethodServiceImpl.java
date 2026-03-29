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
                    
                    CreatePaymentRequest vnpayReq = CreatePaymentRequest.builder()
                            .amount(orderResponse.getTotalDue().longValue())
                            .orderId(orderResponse.getId())
                            .build();

                    CreatePaymentResponse vnpayRes = vnpayService.createPaymentUrl(vnpayReq, httpRequest);

                    return PaymentQRResponse.builder()
                            .paymentTransactionId(null)
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
                    
                    orderClient.sendPaymentResult(PaymentResultRequest.builder()
                            .orderId(orderResponse.getId())
                            .paymentTransactionId(txId)
                            .amount(orderResponse.getTotalDue())
                            .paymentMethod("COD")
                            .status(StatusTransaction.SUCCESS)
                            .build());
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
    public String handleVnpayCallback(Map<String, String> params) {
        try {
            if (vnpayService.validateReturnData(params)) {
                String txnRef = params.get("vnp_TxnRef");
                String[] parts = txnRef.split("_");
                if (parts.length < 2) return null;
                
                String orderIdStr = parts[0];
                com.franchiseproject.paymentservice.dto.response.order.OrderResponse order = orderClient.getOrderInfoByOrderId(UUID.fromString(orderIdStr));
                String typeOrder = order.getTypeOrder();
                String responseCode = params.get("vnp_ResponseCode");

                if ("00".equals(responseCode)) {
                    BigDecimal amount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100));

                    PaymentMethod method = paymentMethodRepository.findByMethodName("VNPAY")
                            .orElseThrow(() -> new RuntimeException("Payment method VNPAY not found"));

                    PaymentTransaction p = PaymentTransaction.builder()
                            .amount(amount)
                            .orderId(UUID.fromString(orderIdStr))
                            .status(StatusTransaction.SUCCESS)
                            .paymentMethod(method)
                            .build();
                    PaymentTransaction savedTx = paymentTransactionService.createPaymentTransaction(p);

                    orderClient.sendPaymentResult(com.franchiseproject.paymentservice.dto.request.PaymentResultRequest.builder()
                            .orderId(UUID.fromString(orderIdStr))
                            .paymentTransactionId(savedTx.getId())
                            .amount(amount)
                            .paymentMethod("VNPAY")
                            .status(StatusTransaction.SUCCESS)
                            .build());
                } else {
                    BigDecimal amount = params.get("vnp_Amount") != null 
                        ? new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100))
                        : BigDecimal.ZERO;

                    PaymentMethod method = paymentMethodRepository.findByMethodName("VNPAY")
                            .orElseThrow(() -> new RuntimeException("Payment method VNPAY not found"));

                    PaymentTransaction p = PaymentTransaction.builder()
                            .amount(amount)
                            .orderId(UUID.fromString(orderIdStr))
                            .status(StatusTransaction.FAILED)
                            .paymentMethod(method)
                            .build();
                    PaymentTransaction savedTx = paymentTransactionService.createPaymentTransaction(p);

                    orderClient.sendPaymentResult(com.franchiseproject.paymentservice.dto.request.PaymentResultRequest.builder()
                            .orderId(UUID.fromString(orderIdStr))
                            .paymentTransactionId(savedTx.getId())
                            .amount(amount)
                            .paymentMethod("VNPAY")
                            .status(StatusTransaction.FAILED)
                            .build());
                }

                return orderIdStr + "|" + typeOrder + "|" + responseCode;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("VNPAY Callback processing failed", e);
        }
    }
}
