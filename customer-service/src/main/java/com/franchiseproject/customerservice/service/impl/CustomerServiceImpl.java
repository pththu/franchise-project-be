package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.client.IdentityClient;
import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.dto.response.UserResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
import com.franchiseproject.customerservice.mapper.CustomerMapper;
import com.franchiseproject.customerservice.repository.CustomerRepository;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    IdentityClient identityClient;

    @Override
    public List<CustomerResponse> getAll(int page) {
        log.info("get all customer");
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by("customerId").ascending()
        );

        Page<CustomerFranchise> customerFranchises = customerRepository.findAll(pageable);
        if (customerFranchises.isEmpty()) return Collections.emptyList();

        return customerFranchises.getContent().stream()
                .map(customer -> {
                    // 1. Map data từ DB sang DTO
                    CustomerResponse response = customerMapper.toCustomerResponse(customer);

                    // 2. Gọi API lấy User (IdentityClient đã sửa lỗi JSON)
                    try {
                        UserResponse user = identityClient.getUserById(customer.getCustomerId());
                        log.info("user: {}", user.getFullName());
                        if (user != null) {
                            response.setUserResponse(user);
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch user for customer ID: {}", customer.getCustomerId());
                    }

                    return response;
                })
                .toList();
    }

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

    @Override
    public PageResponse<CustomerFranchise> getCustomersForAdmin(CustomerStatus status, Pageable pageable) {
        Page<CustomerFranchise> page = (status == null)
                ? customerRepository.findAll(pageable)
                : customerRepository.findByStatus(status, pageable);
        return buildPageResponse(page);
    }

    @Override
    public PageResponse<CustomerFranchise> getCustomersForManager(UUID franchiseId, CustomerStatus status, Pageable pageable) {
        Page<CustomerFranchise> page = (status == null)
                ? customerRepository.findByFranchiseId(franchiseId, pageable)
                : customerRepository.findByFranchiseIdAndStatus(franchiseId, status, pageable);
        return buildPageResponse(page);
    }

    @Override
    public CustomerFranchise getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    @Override
    @Transactional
    public CustomerFranchise createCustomerAtFranchise(UUID customerId, UUID franchiseId, CustomerType type) {
        // customerId này phải được Identity Service trả về (Client tạo account bên Identity xong bắn id qua)
        if (customerRepository.existsByCustomerIdAndFranchiseId(customerId, franchiseId)) {
            throw new AppException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
        }

        CustomerFranchise cf = CustomerFranchise.builder()
                .customerId(customerId)
                .franchiseId(franchiseId)
                .type(type != null ? type : CustomerType.WALK_IN) // Phân biệt loại account
                .status(CustomerStatus.ACTIVE)
                .loyaltyTier(LoyaltyTier.BRONZE)
                .loyaltyCurrentPoint(0)
                .loyaltyTotalPoint(0)
                .build();

        return customerRepository.save(cf);
    }

    @Override
    @Transactional
    public void syncCustomerFromIdentity(UUID customerId, CustomerType type) {
        // User tự đăng ký App -> Không thuộc franchise cụ thể nào ban đầu (franchiseId = null)
        CustomerFranchise cf = CustomerFranchise.builder()
                .customerId(customerId)
                .franchiseId(null)
                .type(type != null ? type : CustomerType.REGISTERED)
                .status(CustomerStatus.ACTIVE)
                .loyaltyTier(LoyaltyTier.BRONZE)
                .loyaltyCurrentPoint(0)
                .loyaltyTotalPoint(0)
                .build();
        customerRepository.save(cf);
    }

    @Override
    @Transactional
    public CustomerFranchise updateCustomerStatus(UUID id, CustomerStatus status) {
        CustomerFranchise customer = getCustomerById(id);
        customer.setStatus(status);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        // Soft Delete
        CustomerFranchise customer = getCustomerById(id);
        customer.setStatus(CustomerStatus.DELETED);
        customerRepository.save(customer);
    }

    private PageResponse<CustomerFranchise> buildPageResponse(Page<CustomerFranchise> pageResult) {
        return PageResponse.<CustomerFranchise>builder()
                .items(pageResult.getContent())
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }
}