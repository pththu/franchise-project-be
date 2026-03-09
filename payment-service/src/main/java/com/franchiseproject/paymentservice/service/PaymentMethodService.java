package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {
    List<PaymentMethod> getAll();
    PaymentMethod create(PaymentMethod paymentMethod);
    PaymentMethod getAvailiablePaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest);
    PaymentQRResponse optionPaymentMethod(OptionPaymentMethodRequest optionPaymentMethodRequest);

}
