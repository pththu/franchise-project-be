package com.franchiseproject.paymentservice.service;

import com.franchiseproject.paymentservice.entity.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethod> getAll();
    PaymentMethod create(PaymentMethod paymentMethod);
}
