package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
import com.franchiseproject.customerservice.mapper.CustomerMapper;
import com.franchiseproject.customerservice.repository.CustomerRepository;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.franchiseproject.customerservice.dto.request.CreateCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SyncCustomerRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

//    @Override
//    public List<CustomerFranchise> getAll() {
//        return customerFranchiseRepository.findAll();
//    }

//    @Override
//    public List<CustomerFranchise> getLoyaltyInfoByCustomerId(UUID customerId) {
//        return customerFranchiseRepository.findByCustomerId(customerId);
//    }

//    @Override
//    public List<Customer> getAll() {
//        return customerRepository.findAll();
//    }

//    @Override
//    public Customer getCustomerById(UUID id) {
//        return customerRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
//    }

//    @Override
//    public PageResponse<CustomerFranchiseResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable) {
//        Page<Customer> pageResult = customerRepository.searchCustomers(keyword, status, pageable);
//
//        List<CustomerFranchiseResponse> customerResponses = pageResult.getContent().stream()
//                .map(customerMapper::toCustomerResponse)
//                .toList();
//
//        return PageResponse.<CustomerFranchiseResponse>builder()
//                .items(customerResponses)
//                .currentPage(pageResult.getNumber())
//                .totalPages(pageResult.getTotalPages())
//                .totalItems(pageResult.getTotalElements())
//                .build();
//    }

//    @Override
//    public PageResponse<CustomerFranchiseResponse> searchAllCustomers(String keyword, CustomerStatus status, Pageable pageable) {
//        Page<Customer> pageResult = customerRepository.searchAllCustomers(keyword, status, pageable);
//        return buildPageResponse(pageResult);
//    }

//    @Override
//    public PageResponse<CustomerFranchiseResponse> searchCustomersByFranchise(UUID franchiseId, String keyword, CustomerStatus status, Pageable pageable) {
//        Page<Customer> pageResult = customerRepository.searchCustomersByFranchise(franchiseId, keyword, status, pageable);
//        return buildPageResponse(pageResult);
//    }

    // LUỒNG 2: Nhân viên tạo khách hàng tại quầy POS
//    @Override
//    @Transactional
//    public CustomerFranchiseResponse createOrLinkCustomerAtFranchise(CreateCustomerRequest request, UUID franchiseId) {
//        Optional<Customer> existingCustomer = customerRepository.findByPhoneOrEmail(request.getPhone(), request.getEmail());
//
//        Customer customer;
//        if (existingCustomer.isPresent()) {
//            customer = existingCustomer.get();
//            if (customerFranchiseRepository.existsByCustomerIdAndFranchiseId(customer.getId(), franchiseId)) {
//                throw new AppException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
//            }
//        } else {
//            customer = Customer.builder()
//                    .fullName(request.getFullName())
//                    .email(request.getEmail())
//                    .phone(request.getPhone())
//                    .status(CustomerStatus.ACTIVE)
//                    .build();
//            customer = customerRepository.save(customer);
//        }
//
//        CustomerFranchise cf = CustomerFranchise.builder()
//                .customer(customer)
//                .franchiseId(franchiseId)
//                .loyaltyTier(LoyaltyTier.BRONZE)
//                .loyaltyCurrentPoint(0)
//                .loyaltyTotalPoint(0)
//                .build();
//        customerFranchiseRepository.save(cf);
//
//        return customerMapper.toCustomerResponse(customer);
//    }

    // LUỒNG 1: App gọi API nội bộ sang để đồng bộ khi khách tự đăng ký User
//    @Override
//    @Transactional
//    public void syncCustomerFromIdentity(SyncCustomerRequest request) {
//        Optional<Customer> existingCustomer = customerRepository.findByPhoneOrEmail(request.getPhone(), request.getEmail());
//
//        if (existingCustomer.isEmpty()) {
//            Customer customer = Customer.builder()
//                    .id(request.getId())
//                    .fullName(request.getFullName())
//                    .email(request.getEmail())
//                    .phone(request.getPhone())
//                    .status(CustomerStatus.ACTIVE)
//                    .build();
//            customerRepository.save(customer);
//        }
//    }

//    @Override
//    public CustomerFranchiseResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
//        Customer customer = getCustomerById(id);
//
//        if (request.getFullName() != null) customer.setFullName(request.getFullName());
//        if (request.getPhone() != null) customer.setPhone(request.getPhone());
//        if (request.getStatus() != null) customer.setStatus(request.getStatus());
//
//        customer = customerRepository.save(customer);
//        return customerMapper.toCustomerResponse(customer);
//    }

//    @Override
//    public void deleteCustomer(UUID id) {
//        Customer customer = getCustomerById(id);
//        customer.setStatus(CustomerStatus.INACTIVE);
//        customerRepository.save(customer);
//    }

//    private PageResponse<CustomerFranchiseResponse> buildPageResponse(Page<Customer> pageResult) {
//        List<CustomerFranchiseResponse> customerResponses = pageResult.getContent().stream()
//                .map(customerMapper::toCustomerResponse)
//                .toList();
//
//        return PageResponse.<CustomerFranchiseResponse>builder()
//                .items(customerResponses)
//                .currentPage(pageResult.getNumber())
//                .totalPages(pageResult.getTotalPages())
//                .totalItems(pageResult.getTotalElements())
//                .build();
//    }
}