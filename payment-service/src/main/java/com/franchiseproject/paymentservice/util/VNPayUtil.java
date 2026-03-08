package com.franchiseproject.paymentservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VNPayUtil {
    // Tạo chữ ký HMAC-SHA512
    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Tạo chuỗi query từ Map (đã sort theo key)
    public static String buildQueryString(Map<String, String> params, boolean encode)
            throws UnsupportedEncodingException {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        for (String field : fieldNames) {
            String value = params.get(field);
            if (value != null && !value.isEmpty()) {
                if (query.length() > 0) query.append("&");
                query.append(field).append("=");
                query.append(encode
                        ? URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                        : value);
            }
        }
        return query.toString();
    }

    // Tạo mã giao dịch unique
    public static String generateTxnRef() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "-" + (int)(Math.random() * 9000 + 1000);
    }

    // Lấy IP của client
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
