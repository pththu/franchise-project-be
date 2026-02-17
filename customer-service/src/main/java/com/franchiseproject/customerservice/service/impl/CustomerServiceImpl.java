package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.repository.CustomerRepository;
import com.franchiseproject.customerservice.repository.specification.CustomerSpecification;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Customer> searchCustomers(String keyword, CustomerStatus status, Pageable pageable) {
        Specification<Customer> spec = CustomerSpecification.filterCustomers(keyword, status);
        return customerRepository.findAll(spec, pageable);
    }
}
