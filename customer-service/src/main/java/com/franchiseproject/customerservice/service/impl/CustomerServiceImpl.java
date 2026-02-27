package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
import com.franchiseproject.customerservice.mapper.CustomerMapper;
import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.repository.CustomerRepository;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    @Override
    public PageResponse<CustomerResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable) {
        Page<Customer> pageResult = customerRepository.searchCustomers(keyword, status, pageable);

        List<CustomerResponse> customerResponses = pageResult.getContent().stream()
                .map(customerMapper::toCustomerResponse)
                .toList();

        return PageResponse.<CustomerResponse>builder()
                .items(customerResponses)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }

    @Override
    public List<UUID> getOrderHistory(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        return List.of(
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    @Override
    public List<UUID> getFeedbackHistory(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        return List.of(
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}
