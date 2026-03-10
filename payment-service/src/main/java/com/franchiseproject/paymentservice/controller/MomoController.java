package com.franchiseproject.paymentservice.controller;

import com.franchiseproject.paymentservice.constant.MomoParameter;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.service.MomoService;
import com.franchiseproject.paymentservice.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/momo")
public class MomoController {

    PaymentTransactionService paymentTransactionService;
    MomoService momoService;

//    @PostMapping("/ipn-handler")
//    public String ipnHandler(@RequestParam Map<String, String> requestParams) {
//        Integer resultCode = Integer.valueOf(requestParams.get(MomoParameter.RESULT_CODE));
//        Long transId = Long.valueOf(requestParams.get(MomoParameter.TRANS_ID));
//        UUID paymentTransactionId = UUID.fromString(String.valueOf(requestParams.get(MomoParameter.REQUEST_ID)));
//        paymentTransactionService.handlePaymentTransaction(transId, paymentTransactionId, resultCode);
//        return resultCode == 0 ? "Giao dịch thành công" : "Giao dịch thất bại";
//    }

    @PostMapping("/ipn-handler")
    public ResponseEntity<String> ipnHandler(@RequestParam Map<String, String> params) {

        boolean validSignature = momoService.verifyIpnSignature(params);

        if (!validSignature) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        Integer resultCode = Integer.valueOf(params.get(MomoParameter.RESULT_CODE));
        Long transId = Long.valueOf(params.get(MomoParameter.TRANS_ID));
        UUID paymentTransactionId = UUID.fromString(params.get(MomoParameter.REQUEST_ID));

        paymentTransactionService.handlePaymentTransaction(transId, paymentTransactionId, resultCode);

        return ResponseEntity.ok("IPN processed");
    }

}
