package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
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
@RequestMapping("/api/payments")
public class PaymentController {
    PaymentMethodService paymentMethodService;

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

}
