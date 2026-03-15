package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.client.MomoClient;
import com.franchiseproject.paymentservice.config.MomoProperties;
import com.franchiseproject.paymentservice.dto.request.CreateMomoRequest;
import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;
import com.franchiseproject.paymentservice.dto.response.OrderResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
//import com.franchiseproject.paymentservice.repository.PaymenMethodRepository;
import com.franchiseproject.paymentservice.repository.PaymentTransactionRepository;
import com.franchiseproject.paymentservice.service.MomoService;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MomoServiceImpl implements MomoService {

    PaymentTransactionService paymentTransactionService;
    PaymentTransactionRepository paymentTransactionRepository;
    MomoProperties momoProperties;
    MomoClient momoClient;

    /// Dùng build request để tạo giao dịch MOMO
    @Override
    @Transactional
    public CreateMomoResponse buildCreateMomoQR(OrderResponse orderResponse, PaymentMethod paymentMethod) {

        String prettySignature = "";
        String orderInfo = "ThanhToanHoaDon_" + orderResponse.getOrderId();
        long amount = orderResponse.getFinalTotal().longValueExact();

        PaymentTransaction paymentTransaction = paymentTransactionService.buildPaymentTransaction(orderResponse, paymentMethod);
        paymentTransaction.changeStatus(StatusTransaction.PENDING);
        paymentTransactionRepository.save(paymentTransaction);

        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                momoProperties.getAccess_key(), amount, "", momoProperties.getIpn_url(),
                orderResponse.getOrderId(), orderInfo, momoProperties.getPartner_code(),
                momoProperties.getReturn_url(), paymentTransaction.getId(), momoProperties.getRequest_type());

        log.info("MoMo rawSignature: {}", rawSignature);
        try {
            prettySignature = signHmacSHA256(rawSignature, momoProperties.getSecret_key());
        } catch (Exception e) {
            log.error("Error signing HMACSHA256", e);
            throw new AppException(ErrorCode.SIGNATURE_FAILED);
        }
        log.info("MoMo signature: {}", prettySignature);
        if (prettySignature.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }
        CreateMomoRequest request = CreateMomoRequest.builder()
                .partnerCode(momoProperties.getPartner_code())
                .requestType(momoProperties.getRequest_type())
                .ipnUrl(momoProperties.getIpn_url())
                .redirectUrl(momoProperties.getReturn_url())
                .orderId(orderResponse.getOrderId().toString())
                .orderInfo(orderInfo)
                .requestId(paymentTransaction.getId().toString())
                .extraData("")
                .amount(amount)
                .signature(prettySignature)
                .lang("vi")
                .build();


        return momoClient.createMomoQR(request);
    }

    /// Dùng tạo signature
    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /// Xác nhận signature từ INP để chắc chắn là đúng giao dịch
    @Override
    @Transactional
    public boolean verifyIpnSignature(Map<String, String> params) {

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                momoProperties.getAccess_key(),
                params.get("amount"),
                params.get("extraData"),
                params.get("message"),
                params.get("orderId"),
                params.get("orderInfo"),
                params.get("orderType"),
                params.get("partnerCode"),
                params.get("payType"),
                params.get("requestId"),
                params.get("responseTime"),
                params.get("resultCode"),
                params.get("transId")
        );
        try {
            String generatedSignature = signHmacSHA256(rawSignature, momoProperties.getSecret_key());
            String momoSignature = params.get("signature");
            return generatedSignature.equals(momoSignature);
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

}
