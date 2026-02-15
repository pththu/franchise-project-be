package com.franchiseproject.paymentservice.service.impl;

import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.repository.PaymenMethodRepository;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    PaymenMethodRepository  paymenMethodRepository;


    @Override
    public List<PaymentMethod> getAll() {
        return paymenMethodRepository.findAll();
    }

    @Override
    public PaymentMethod create(PaymentMethod paymentMethod) {
        return paymenMethodRepository.save(paymentMethod);
    }
}
