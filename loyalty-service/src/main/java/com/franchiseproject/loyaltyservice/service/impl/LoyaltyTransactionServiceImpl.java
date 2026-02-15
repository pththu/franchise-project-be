package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {
    LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Override
    public List<LoyaltyTransaction> getByCustomerId(UUID customerId) {
        return loyaltyTransactionRepository.findLoyaltyTransactionsByCustomerId(customerId);
    }
}
