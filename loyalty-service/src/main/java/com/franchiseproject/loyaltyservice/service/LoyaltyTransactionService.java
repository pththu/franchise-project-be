package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;

import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionService {
    List<LoyaltyTransaction> getByCustomerId (UUID customerId);
}
