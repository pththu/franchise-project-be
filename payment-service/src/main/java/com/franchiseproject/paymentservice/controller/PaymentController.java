package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    PaymentMethodService  paymentMethodService;

    @GetMapping("/getAll")
    public List<PaymentMethod> getAll(){
        return paymentMethodService.getAll();
    }

    @PostMapping("/save")
    public PaymentMethod save(@RequestBody PaymentMethod paymentMethod){
        return paymentMethodService.create(paymentMethod);
    }

}
