package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.request.AdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getLoyaltyTransactionByCustomerId(@PathVariable("customerId") UUID customerId) {
        Map<String, Object> response = new HashMap<>();

        List<LoyaltyTransaction> loyaltyTransactions = loyaltyTransactionService.getByCustomerId(customerId);
        response.put("message", "Get All loyaltyTransactions");
        response.put("data", loyaltyTransactions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/adjust")
    public ResponseEntity<AdjustPointsResponse> adjustPoints(@RequestBody @Valid AdjustPointsRequest request) {
        AdjustPointsResponse response = loyaltyTransactionService.adjustPoints(request);
        return ResponseEntity.ok(response);
    }
}