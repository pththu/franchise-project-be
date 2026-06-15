package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.dto.request.SaveCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SearchRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.CustomerSummaryResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<CustomerFranchiseResponse> getAll(int page);

    // READ
    PageResponse<CustomerFranchiseResponse> getCustomersForAdmin(CustomerStatus status, Pageable pageable);

    PageResponse<CustomerFranchiseResponse> getCustomersForManager(UUID franchiseId, CustomerStatus status, Pageable pageable);

    // READ & SEARCH
    CustomerFranchiseResponse getCustomerById(UUID id);

    CustomerFranchiseResponse getCustomerOfFranchiseById(UUID userId, UUID franchiseId);

    PageResponse<CustomerFranchiseResponse> searchCustomers(UUID franchiseId, CustomerStatus status, List<UUID> customerIds, Pageable pageable);
    List<CustomerFranchiseResponse> getCustomersByIds(List<UUID> ids);

    Page<CustomerSummaryResponse> searchCustomers(SearchRequest request);

    // CREATE / SYNC
//    CustomerFranchise createCustomerAtFranchise(UUID customerId, UUID franchiseId, CustomerType type);
    CustomerFranchise saveCustomerFranchise(SaveCustomerRequest request);

    void syncCustomerFromIdentity(UUID customerId, CustomerType type);

    // UPDATE & DELETE
    CustomerFranchise updateCustomerFranchise(UUID id, UpdateCustomerRequest request);

    CustomerFranchise updateCustomerStatus(UUID id, CustomerStatus status);

    void deleteCustomer(UUID id);
}