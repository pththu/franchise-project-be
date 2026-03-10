package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.exception.AppException;
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
    PaymentMethodService paymentMethodService;
    PaymentTransactionService paymentTransactionService;

    @GetMapping("/getAll")
    public List<PaymentMethod> getAll() {
        return paymentMethodService.getAll();
    }

    @PostMapping("/save")
    public PaymentMethod save(@RequestBody PaymentMethod paymentMethod) {
        return paymentMethodService.create(paymentMethod);
    }

    @PostMapping("/option")
    public ApiResponse<PaymentQRResponse> optionPaymentMethod(@RequestBody OptionPaymentMethodRequest optionPaymentMethodRequest) {
        PaymentQRResponse paymentQRResponse = paymentMethodService.optionPaymentMethod(optionPaymentMethodRequest);
        return ApiResponse.<PaymentQRResponse>builder()
                .message("Payment Method has been saved successfully!")
                .data(paymentQRResponse)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @GetMapping("/{orderId}/get-transaction")
    public ApiResponse<PaymentTransactionResponse> getPaymentTransactionByOrderId(@PathVariable UUID orderId) {
        PaymentTransactionResponse paymentTransactionResponse = paymentTransactionService.getPaymentTransactionByOrderId(orderId);
        return ApiResponse.<PaymentTransactionResponse>builder()
                .message("Get Transaction by OrderId Success!")
                .data(paymentTransactionResponse)
                .statusCode(200)
                .errors(null)
                .build();
    }

}
