package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.repository.CustomerRepository;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }
}
