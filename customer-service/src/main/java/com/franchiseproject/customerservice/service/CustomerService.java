package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.dto.request.CreateCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SyncCustomerRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<Customer> getAll();

    Customer getCustomerById(UUID id);

    PageResponse<CustomerResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable);

    PageResponse<CustomerResponse> searchAllCustomers(String keyword, CustomerStatus status, Pageable pageable);

    PageResponse<CustomerResponse> searchCustomersByFranchise(UUID franchiseId, String keyword, CustomerStatus status, Pageable pageable);

    // Nhân viên tạo tại quầy
    CustomerResponse createOrLinkCustomerAtFranchise(CreateCustomerRequest request, UUID franchiseId);

    // Hệ thống đồng bộ khi user đăng ký
    void syncCustomerFromIdentity(SyncCustomerRequest request);

    CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request);

    void deleteCustomer(UUID id);
}