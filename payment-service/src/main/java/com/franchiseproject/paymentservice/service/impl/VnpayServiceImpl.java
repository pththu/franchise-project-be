package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.config.VNPayConfig;
import com.franchiseproject.paymentservice.dto.request.CreatePaymentRequest;
import com.franchiseproject.paymentservice.dto.response.CreatePaymentResponse;
import com.franchiseproject.paymentservice.service.VnpayService;
import com.franchiseproject.paymentservice.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnpayServiceImpl implements VnpayService {
    VNPayConfig vnPayConfig;

    @Override
    public CreatePaymentResponse createPaymentUrl(CreatePaymentRequest request,
                                                  HttpServletRequest httpRequest,
                                                  java.util.UUID paymentTransactionId) throws Exception {
        String txnRef = request.getOrderId().toString() + "_" + (paymentTransactionId != null ? paymentTransactionId.toString() : VNPayUtil.generateTxnRef());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String createDate = formatter.format(new Date());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        String orderInfo = "test123";

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf((long) request.getAmount() * 100));
        params.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnPayConfig.getLocale());
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String queryString = VNPayUtil.buildQueryString(params, true);
        String secureHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getHashSecret(),
                queryString
        );
        String paymentUrl = vnPayConfig.getPaymentUrl() + "?"
                + queryString
                + "&vnp_SecureHashType=HmacSHA512"
                + "&vnp_SecureHash=" + secureHash;

        return CreatePaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .txnRef(txnRef)
                .build();
    }

    @Override
    public boolean validateReturnData(Map<String, String> params) throws Exception {
        Map<String, String> fields = new HashMap<>(params);
        String receivedHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        String hashData = VNPayUtil.buildQueryString(fields, false);
        String calculatedHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData
        );
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }
}
