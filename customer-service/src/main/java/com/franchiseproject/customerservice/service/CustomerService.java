package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.dto.request.CreateCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SyncCustomerRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
//    List<CustomerFranchise> getAll();

//    CustomerFranchise getCustomerById(UUID id);

//    PageResponse<CustomerFranchiseResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable);

//    PageResponse<CustomerFranchiseResponse> searchAllCustomers(String keyword, CustomerStatus status, Pageable pageable);

//    PageResponse<CustomerFranchiseResponse> searchCustomersByFranchise(UUID franchiseId, String keyword, CustomerStatus status, Pageable pageable);

    // Nhân viên tạo tại quầy
//    CustomerFranchiseResponse createOrLinkCustomerAtFranchise(CreateCustomerRequest request, UUID franchiseId);
//
//    // Hệ thống đồng bộ khi user đăng ký
//    void syncCustomerFromIdentity(SyncCustomerRequest request);
//
//    CustomerFranchiseResponse updateCustomer(UUID id, UpdateCustomerRequest request);
//
//    void deleteCustomer(UUID id);
//    List<CustomerFranchise> getAll();
//
//    List<CustomerFranchise> getLoyaltyInfoByCustomerId(UUID customerId);
}