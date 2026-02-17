package com.franchiseproject.customerservice.service;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<Customer> getAll();

    Customer getCustomerById(UUID id);

    Page<Customer> searchCustomers(String keyword, CustomerStatus status, Pageable pageable);
}
