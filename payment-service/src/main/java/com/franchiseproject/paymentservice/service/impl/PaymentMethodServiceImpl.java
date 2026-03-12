package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.OrderClient;
import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;
import com.franchiseproject.paymentservice.dto.response.OrderResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentMethodResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
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
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    PaymentMethodRepository paymentMethodRepository;
    PaymentTransactionService paymentTransactionService;
    PaymentMethodMapper paymentMethodMapper;
    OrderClient orderClient;
    MomoService momoService;

    @Override
    public List<PaymentMethod> getAll() {
        return paymentMethodRepository.findAll();
    }

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

    @Override
    @Transactional
    public PaymentMethod getAvailiablePaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest) {
        return paymentMethodRepository
                .findByIdAndActiveTrue(optionPaymentMethodRequest.getPaymentMethodId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.PAYMENT_METHOD_NOT_AVAILABLE));
    }

    @Override
    @Transactional
    public PaymentQRResponse optionPaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest) {

        OrderResponse orderResponse = orderClient.getOrderInfoByOrderId(optionPaymentMethodRequest.getOrderId());
        PaymentMethod paymentMethod = getAvailiablePaymentMethod(optionPaymentMethodRequest);
        paymentTransactionService.checkDuplicateTransaction(orderResponse);
        switch (paymentMethod.getMethodName()) {
            case "MOMO":
                CreateMomoResponse createMomoResponse = momoService.buildCreateMomoQR(orderResponse, paymentMethod);
                return PaymentQRResponse.builder()
                        .paymentTransactionId(UUID.fromString(createMomoResponse.getRequestId()))
                        .method("MOMO")
                        .paymentUrl(createMomoResponse.getPayUrl())
                        .qrCodeUrl(createMomoResponse.getQrCodeUrl())
                        .amount(createMomoResponse.getAmount())
                        .build();
            case "VNPAY":
            case "COD":
            default:
                throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
    }
}
