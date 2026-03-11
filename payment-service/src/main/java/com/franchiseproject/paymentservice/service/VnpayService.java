package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.request.CreatePaymentRequest;
import com.franchiseproject.paymentservice.dto.response.CreatePaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnpayService {
    CreatePaymentResponse createPaymentUrl(CreatePaymentRequest request, HttpServletRequest httpRequest) throws Exception;
    boolean validateReturnData(Map<String, String> params) throws Exception;
}
