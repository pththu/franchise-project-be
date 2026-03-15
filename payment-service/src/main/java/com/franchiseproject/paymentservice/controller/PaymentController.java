package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentMethodResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentQRResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.validation.Valid;
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

    /// resolve request option payment method(COD, MOMO, VNPAY)
    @PostMapping("/option")
    public ApiResponse<PaymentQRResponse> optionPaymentMethod(@Valid @RequestBody OptionPaymentMethodRequest optionPaymentMethodRequest) {
        PaymentQRResponse paymentQRResponse = paymentMethodService.optionPaymentMethod(optionPaymentMethodRequest);
        return ApiResponse.<PaymentQRResponse>builder()
                .message("Choice Payment Method and create QR successfully!")
                .data(paymentQRResponse)
                .statusCode(200)
                .errors(null)
                .build();
    }

    /// Lấy giao dịch theo orderId
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

    /// Lấy các phương thức thanh toán khả dụng
    @GetMapping("/get-method")
    public ApiResponse<List<PaymentMethodResponse>> getPaymentMethodByOrderId() {
        return ApiResponse.<List<PaymentMethodResponse>>builder()
                .message("Lấy danh sách payment method khả dụng thành công")
                .data(paymentMethodService.getAllByActiveTrue())
                .statusCode(200)
                .errors(null)
                .build();
    }

    /// Lấy trạng thái của giao dịch theo orderId
    @GetMapping("/status/{orderId}")
    public ApiResponse<StatusTransaction> checkStatus(@PathVariable UUID orderId) {
        return ApiResponse.<StatusTransaction>builder()
                .message("Lấy trạng thái Transaction thành công!")
                .data(paymentTransactionService.getPaymentTransactionByOrderId(orderId).getStatus())
                .statusCode(200)
                .build();
    }

}
