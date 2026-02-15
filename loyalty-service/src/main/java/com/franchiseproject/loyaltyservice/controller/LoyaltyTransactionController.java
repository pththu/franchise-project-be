package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyTransactionController {
    LoyaltyTransactionService loyaltyTransactionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLoyaltyTransactionByCustomerId() {
        Map<String, Object> response = new HashMap<>();

        List<LoyaltyTransaction> loyaltyTransactions = loyaltyTransactionService.getByCustomerId(UUID.randomUUID());
        response.put("message", "Get All loyaltyTransactions");
        response.put("data", loyaltyTransactions);

        return ResponseEntity.ok(response);
    }
}
