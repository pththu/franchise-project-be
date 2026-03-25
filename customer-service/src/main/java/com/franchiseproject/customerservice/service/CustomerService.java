package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    // READ & SEARCH
    CustomerFranchise getCustomerById(UUID id);

    PageResponse<CustomerFranchise> searchCustomers(UUID franchiseId, CustomerStatus status, List<UUID> customerIds, Pageable pageable);

    // CREATE / SYNC
    CustomerFranchise createCustomerAtFranchise(UUID customerId, UUID franchiseId, CustomerType type);

    void syncCustomerFromIdentity(UUID customerId, CustomerType type);

    // UPDATE & DELETE
    CustomerFranchise updateCustomerFranchise(UUID id, UpdateCustomerRequest request);

    CustomerFranchise updateCustomerStatus(UUID id, CustomerStatus status);

    void deleteCustomer(UUID id);
}