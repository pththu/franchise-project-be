package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.dto.request.OptionPaymentMethodRequest;
import com.franchiseproject.paymentservice.dto.response.*;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import com.franchiseproject.paymentservice.service.PaymentMethodService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {
    PaymentMethodService paymentMethodService;
    PaymentTransactionService paymentTransactionService;

    @Value("${momo.return_url}")
    @NonFinal
    String feReturnUrl;

    @GetMapping("/getAll")
    public List<PaymentMethod> getAll() {
        return paymentMethodService.getAll();
    }

    @PostMapping("/save")
    public PaymentMethod save(@RequestBody PaymentMethod paymentMethod) {
        return paymentMethodService.create(paymentMethod);
    }

    /// resolve request option payment method(COD, MOMO, VNPAY)
    @PostMapping("/init")
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
        try {
            PaymentTransactionResponse paymentTransactionResponse = paymentTransactionService.getPaymentTransactionByOrderId(orderId);
            return ApiResponse.<PaymentTransactionResponse>builder()
                    .message("Get Transaction by OrderId Success!")
                    .data(paymentTransactionResponse)
                    .statusCode(200)
                    .errors(null)
                    .build();
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_TRANSACTION) {
                return ApiResponse.<PaymentTransactionResponse>builder()
                        .message("No online transaction for this order")
                        .data(null)
                        .statusCode(200)
                        .errors(null)
                        .build();
            }
            throw e;
        }
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
        try {
            return ApiResponse.<StatusTransaction>builder()
                    .message("Lấy trạng thái Transaction thành công!")
                    .data(paymentTransactionService.getPaymentTransactionByOrderId(orderId).getStatus())
                    .statusCode(200)
                    .build();
        } catch (com.franchiseproject.paymentservice.exception.AppException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_TRANSACTION) {
                return ApiResponse.<StatusTransaction>builder()
                        .message("Transaction not found or deleted")
                        .data(StatusTransaction.CANCELLED)
                        .statusCode(200)
                        .build();
            }
            throw e;
        }
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        String result = null;
        try {
            result = paymentMethodService.handleVnpayCallback(params);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.info("VNPay callback optimistic locking conflict (already processed).");
            // Try to recover basic info from params if possible
            String txnRef = params.get("vnp_TxnRef");
            if (txnRef != null) {
                String orderIdStr = txnRef.split("_")[0];
                result = orderIdStr + "|POS|" + params.get("vnp_ResponseCode");
            }
        } catch (Exception e) {
            log.error("VNPay callback failed", e);
        }

        if (result != null && !result.isEmpty()) {
            String[] parts = result.split("\\|");
            String orderId = parts[0];
            String typeOrder = parts[1];

            String redirectUrl = feReturnUrl;
            if ("POS".equalsIgnoreCase(typeOrder)) {
                // Get base URL (e.g., http://localhost:5173) from feReturnUrl
                try {
                    java.net.URI uri = new java.net.URI(feReturnUrl);
                    String baseUrl = uri.getScheme() + "://" + uri.getHost();
                    if (uri.getPort() != -1) baseUrl += ":" + uri.getPort();
                    redirectUrl = baseUrl + "/staff/order-success";
                } catch (Exception e) {
                    // Fallback to replace if URI parsing fails
                    redirectUrl = feReturnUrl.replace("/order-success", "/staff/order-success");
                }
            }

            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "orderId=" + orderId;
            response.sendRedirect(redirectUrl);
        } else {
            // Even if result is null (e.g. signature validation failed), try to extract orderId for better UX
            String redirectUrlFallback = feReturnUrl;
            String txnRef = params.get("vnp_TxnRef");
            if (txnRef != null) {
                String orderIdFallback = txnRef.split("_")[0];
                redirectUrlFallback += (redirectUrlFallback.contains("?") ? "&" : "?") + "orderId=" + orderIdFallback;
            }
            response.sendRedirect(redirectUrlFallback);
        }
    }

    @DeleteMapping("/internal/transactions/order/{orderId}")
    public ApiResponse<Void> deleteTransactionByOrderId(@PathVariable UUID orderId) {
        paymentTransactionService.deleteTransactionByOrderId(orderId);
        return ApiResponse.<Void>builder()
                .message("Transaction deleted")
                .statusCode(200)
                .build();
    }

    @GetMapping("/pay-url/{orderId}")
    public ApiResponse<RePaymentResponse> payUrl(@PathVariable UUID orderId) {
        RePaymentResponse response = paymentTransactionService.getPayUrl(orderId);
        return ApiResponse.<RePaymentResponse>builder()
                .message("Payment URL get success")
                .data(response)
                .statusCode(200)
                .build();
    }

}
