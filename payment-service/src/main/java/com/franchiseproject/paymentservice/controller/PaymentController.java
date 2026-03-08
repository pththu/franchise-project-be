package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.request.CreatePaymentRequest;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.dto.response.CreatePaymentResponse;
import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.dto.response.VnpayReturnResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import com.franchiseproject.paymentservice.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    PaymentMethodService  paymentMethodService;
    PaymentTransactionService  paymentTransactionService;
    VnpayService  vnpayService;

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

    // Tạo URL thanh toán (VNPAY)
    @PostMapping("/create")
    public ApiResponse<CreatePaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) throws Exception {
        return ApiResponse.<CreatePaymentResponse>builder()
                .statusCode(200)
                .message("Tạo link thanh toán thành công")
                .data(vnpayService.createPaymentUrl(request, httpRequest))
                .build();
    }

    //VNPay redirect về sau khi user thanh toán
    @GetMapping("/vnpay-return")
    public ApiResponse<VnpayReturnResponse> vnpayReturn(
            @RequestParam Map<String, String> params) throws Exception {
        boolean isValid = vnpayService.validateReturnData(new HashMap<>(params));
        if (!isValid) {
            return ApiResponse.<VnpayReturnResponse>builder()
                    .statusCode(400)
                    .message("Chữ ký không hợp lệ")
                    .build();
        }
        String responseCode = params.get("vnp_ResponseCode");
        boolean isSuccess   = "00".equals(responseCode);
        VnpayReturnResponse data = VnpayReturnResponse.builder()
                .txnRef(params.get("vnp_TxnRef"))
                .amount(params.get("vnp_Amount"))
                .responseCode(responseCode)
                .build();
        return ApiResponse.<VnpayReturnResponse>builder()
                .statusCode(isSuccess ? 200 : 400)
                .message(isSuccess ? "Thanh toán thành công" : "Thanh toán thất bại")
                .data(data)
                .build();
    }

    //VNPay gọi IPN (server-to-server)
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIPN(
            @RequestParam Map<String, String> params) throws Exception {
        boolean isValid = vnpayService.validateReturnData(new HashMap<>(params));
        if (!isValid) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "97",
                    "Message", "Invalid Signature"
            ));
        }
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "RspCode", "00",
                "Message", "Order Failed"
        ));
    }
}
