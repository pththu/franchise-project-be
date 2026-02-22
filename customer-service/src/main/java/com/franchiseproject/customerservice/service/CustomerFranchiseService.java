package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.model.CustomerFranchise;

import java.util.List;
import java.util.UUID;

public interface CustomerFranchiseService {
    List<CustomerFranchise> getAll();

    List<CustomerFranchise> getLoyaltyInfoByCustomerId(UUID customerId);
}
