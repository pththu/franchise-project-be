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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnpayServiceImpl implements VnpayService {

    VNPayConfig vnPayConfig;

    @Override
    public CreatePaymentResponse createPaymentUrl(CreatePaymentRequest request,
                                            HttpServletRequest httpRequest) throws Exception {
        String txnRef = VNPayUtil.generateTxnRef();
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String orderInfo = "Thanh toan don hang " + request.getOrderId()
                + " - User: " + request.getUserId();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version",     vnPayConfig.getVersion());
        params.put("vnp_Command",     vnPayConfig.getCommand());
        params.put("vnp_TmnCode",     vnPayConfig.getTmnCode());
        params.put("vnp_Amount",      String.valueOf(request.getAmount() * 100));
        params.put("vnp_CurrCode",    vnPayConfig.getCurrencyCode());
        params.put("vnp_TxnRef",      txnRef);
        params.put("vnp_OrderInfo",   orderInfo);
        params.put("vnp_OrderType",   "other");
        params.put("vnp_Locale",      vnPayConfig.getLocale());
        params.put("vnp_ReturnUrl",   vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr",      VNPayUtil.getIpAddress(httpRequest));
        params.put("vnp_CreateDate",  createDate);

        // Tạo chuỗi hash (KHÔNG encode)
        String hashData = VNPayUtil.buildQueryString(params, false);
        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

        // Tạo URL đầy đủ (encode)
        String queryString = VNPayUtil.buildQueryString(params, true);
        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + queryString
                + "&vnp_SecureHash=" + secureHash;

        return CreatePaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .txnRef(txnRef)
                .build();
    }

    @Override
    public boolean validateReturnData(Map<String, String> params) throws Exception {
        String receivedHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        String hashData = VNPayUtil.buildQueryString(params, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }
}
