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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MomoServiceImpl implements MomoService {

    PaymentTransactionRepository paymentTransactionRepository;
    MomoProperties momoProperties;
    MomoClient momoClient;

    @Override
    @Transactional
    public CreateMomoResponse buildCreateMomoQR(OrderResponse orderResponse, PaymentMethod paymentMethod) {

        String orderInfo = "Thanh Toán Đơn Hàng: " + orderResponse.getOrderId();

        checkOrderStatus(orderResponse);

        PaymentTransaction paymentTransaction = buildPaymentTransaction(orderResponse, paymentMethod);
        paymentTransactionRepository.save(paymentTransaction);

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                momoProperties.getAccess_key(), orderResponse.getFinalTotal().longValueExact(), "",
                momoProperties.getIpn_url(), orderResponse.getOrderId().toString(), orderInfo, momoProperties.getPartner_code(),
                momoProperties.getReturn_url(), paymentTransaction.getId().toString(), momoProperties.getRequest_type());

        String prettySignature = "";

        try {
            prettySignature = signHmacSHA256(rawSignature, momoProperties.getSecret_key());
        } catch (Exception e) {
            log.error("Error signing HMACSHA256" + e);
            return null;
        }

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
                .amount(orderResponse.getFinalTotal().longValueExact())
                .signature(prettySignature)
                .lang("vi")
                .build();



        return momoClient.createMomoQR(request);
    }

    private PaymentTransaction buildPaymentTransaction(OrderResponse orderResponse, PaymentMethod paymentMethod) {
        return PaymentTransaction.builder()
                .userId(orderResponse.getCustomerId())
                .orderId(orderResponse.getOrderId())
                .amount(orderResponse.getFinalTotal())
                .status(StatusTransaction.PENDING)
                .paymentMethod(paymentMethod)
                .transactionRef(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

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

    private void checkOrderStatus(OrderResponse orderResponse) {
        if (!orderResponse.getOrderStatus().equals("WAITING_PAYMENT")) {
            throw new AppException(ErrorCode.ORDER_NOT_PAYABLE);
        }
    }

    public boolean verifySignature(String rawData, String signature, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(rawData.getBytes());
            String generatedSignature = Base64.getEncoder().encodeToString(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }

}
