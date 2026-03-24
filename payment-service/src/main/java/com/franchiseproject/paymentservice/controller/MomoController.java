package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.constant.MomoParameter;
import com.franchiseproject.paymentservice.dto.response.ApiResponse;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import com.franchiseproject.paymentservice.service.MomoService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/momo")
public class MomoController {

    PaymentTransactionService paymentTransactionService;
    MomoService momoService;
    com.franchiseproject.paymentservice.client.OrderClient orderClient;

    @org.springframework.beans.factory.annotation.Value("${momo.return_url}")
    @lombok.experimental.NonFinal
    String feReturnUrl;

    /// Request từ MOMO đến payment-service để xử lý kết quả
    @PostMapping("/ipn-handler")
    public ResponseEntity<String> ipnHandler(@RequestBody Map<String, String> params) {
        System.out.println("MoMo IPN called: " + params);
        try {
            boolean validSignature = momoService.verifyIpnSignature(params);

            System.out.println("Signature valid = " + validSignature);

            if (!validSignature) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            String resultCodeStr = params.get(MomoParameter.RESULT_CODE);
            String transIdStr = params.get(MomoParameter.TRANS_ID);
            String requestIdStr = params.get(MomoParameter.REQUEST_ID);
            if (resultCodeStr == null || transIdStr == null || requestIdStr == null) {
                return ResponseEntity.badRequest().body("Missing parameters");
            }

            System.out.println("resultCode=" + resultCodeStr);
            System.out.println("transId=" + transIdStr);
            System.out.println("requestId=" + requestIdStr);

            Integer resultCode = Integer.valueOf(resultCodeStr);
            Long transId = Long.valueOf(transIdStr);
            UUID paymentTransactionId = UUID.fromString(requestIdStr);

            System.out.println("Calling service...");

            paymentTransactionService.handlePaymentTransaction(transId, paymentTransactionId, resultCode);
            return ResponseEntity.ok("IPN processed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("IPN processing error");
        }
    }

    /// Synchronous Redirect from Momo
    @org.springframework.web.bind.annotation.GetMapping("/return")
    public void momoReturn(@org.springframework.web.bind.annotation.RequestParam java.util.Map<String, String> params, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        System.out.println("MoMo Return called: " + params);
        String orderIdStr = params.get("orderId");
        String typeOrder = "ONLINE";

        try {
//            boolean validSignature = momoService.verifyIpnSignature(params);
//            if (validSignature) {
//                String resultCodeStr = params.get("resultCode");
//                String transIdStr = params.get("transId");
//                String requestIdStr = params.get("requestId");
//
//                if (resultCodeStr != null && transIdStr != null && requestIdStr != null) {
//                    Integer resultCode = Integer.valueOf(resultCodeStr);
//                    Long transId = Long.valueOf(transIdStr);
//                    java.util.UUID paymentTransactionId = java.util.UUID.fromString(requestIdStr);
//
//                    paymentTransactionService.handlePaymentTransaction(transId, paymentTransactionId, resultCode);
//                }
//            }
//
            if (orderIdStr != null) {
                com.franchiseproject.paymentservice.dto.response.order.OrderResponse order = orderClient.getOrderInfoByOrderId(java.util.UUID.fromString(orderIdStr));
                if (order != null) {
                    typeOrder = order.getTypeOrder();
                }
            }
        } catch (Exception e) {
             e.printStackTrace();
        }

        String redirectUrl = "POS".equalsIgnoreCase(typeOrder) 
                ? feReturnUrl.replace("/order-success", "/staff/order-success") 
                : feReturnUrl;
                
        if (orderIdStr != null) {
            redirectUrl += "?orderId=" + orderIdStr;
        }
        
        response.sendRedirect(redirectUrl);
    }

}
