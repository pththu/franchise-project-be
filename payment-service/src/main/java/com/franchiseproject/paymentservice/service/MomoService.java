package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;
import com.franchiseproject.paymentservice.dto.response.order.OrderResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;

import java.util.Map;

public interface MomoService {
    CreateMomoResponse buildCreateMomoQR(OrderResponse orderResponse, PaymentMethod paymentMethod);

    boolean verifyIpnSignature(Map<String, String> params);
}
