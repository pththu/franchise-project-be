package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.UpdateRuleRequest;
import com.franchiseproject.loyaltyservice.model.LoyaltyRule;
import com.franchiseproject.loyaltyservice.repository.LoyaltyRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyRuleServiceImpl {

    LoyaltyRuleRepository loyaltyRuleRepository;

    @PostConstruct
    public void initRule() {
        if (loyaltyRuleRepository.count() == 0) {
            loyaltyRuleRepository.save(new LoyaltyRule(1L, 10000.0));
        }
    }

    public Double getAmountPerPoint() {
        return loyaltyRuleRepository.findById(1L)
                .map(LoyaltyRule::getAmountPerPoint)
                .orElse(10000.0);
    }

    public void updateRule(UpdateRuleRequest request) {
        LoyaltyRule rule = loyaltyRuleRepository.findById(1L)
                .orElse(new LoyaltyRule(1L, 10000.0));
        rule.setAmountPerPoint(request.getAmountPerPoint());
        loyaltyRuleRepository.save(rule);
    }
}