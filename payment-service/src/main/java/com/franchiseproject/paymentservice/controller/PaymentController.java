package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    PaymentMethodService  paymentMethodService;
    PaymentTransactionService  paymentTransactionService;

    @GetMapping("/getAll")
    public List<PaymentMethod> getAll(){
        return paymentMethodService.getAll();
    }

    @PostMapping("/save")
    public PaymentMethod save(@RequestBody PaymentMethod paymentMethod){
        return paymentMethodService.create(paymentMethod);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<PaymentTransactionResponse>> getByUserId(
            @PathVariable UUID userId){
        return ApiResponse.<List<PaymentTransactionResponse>>builder()
                .message("Get payments by user id successfully!")
                .data(paymentTransactionService.getTransactionsByUserId(userId))
                .build();
    }
}
