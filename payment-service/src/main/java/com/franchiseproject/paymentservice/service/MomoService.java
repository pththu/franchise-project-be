package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.request.CreateMomoRequest;
import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.request.PaymentTransactionRequest;
import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;

public interface MomoService {
    CreateMomoResponse buildCreateMomoQR(PaymentTransactionRequest paymentTransactionRequest, OptionPaymentMethodRequest optionPaymentMethodRequest);
}
