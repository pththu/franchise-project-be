package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.client.IdentityClient;
import com.franchiseproject.customerservice.client.LoyaltyClient;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.dto.response.UserResponse;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
import com.franchiseproject.customerservice.mapper.CustomerFranchiseMapper;
import com.franchiseproject.customerservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

    CustomerFranchiseRepository customerFranchiseRepository;
    CustomerFranchiseMapper customerFranchiseMapper;
    IdentityClient identityClient;
    LoyaltyClient loyaltyClient;

    @Override
    public List<CustomerFranchiseResponse> getAll(int page) {
        log.info("Get all customer franchises");
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").ascending());
        Page<CustomerFranchise> customerFranchises = customerFranchiseRepository.findAll(pageable);

        return buildPageResponse(customerFranchises).getItems();
    }

    // ================== READ & SEARCH ==================

    @Override
    public PageResponse<CustomerFranchiseResponse> getCustomersForAdmin(CustomerStatus status, Pageable pageable) {
        Page<CustomerFranchise> page = (status == null)
                ? customerFranchiseRepository.findAll(pageable)
                : customerFranchiseRepository.findByStatus(status, pageable);
        return buildPageResponse(page);
    }

    @Override
    public PageResponse<CustomerFranchiseResponse> getCustomersForManager(UUID franchiseId, CustomerStatus status, Pageable pageable) {
        Page<CustomerFranchise> page = (status == null)
                ? customerFranchiseRepository.findByFranchiseId(franchiseId, pageable)
                : customerFranchiseRepository.findByFranchiseIdAndStatus(franchiseId, status, pageable);
        return buildPageResponse(page);
    }

    @Override
    public PageResponse<CustomerFranchiseResponse> searchCustomers(UUID franchiseId, CustomerStatus status, List<UUID> userIds, Pageable pageable) {
        boolean filterByUserIds = userIds != null && !userIds.isEmpty();

        if (userIds != null && userIds.isEmpty()) {
            return buildPageResponse(Page.empty(pageable));
        }

        Page<CustomerFranchise> page = customerFranchiseRepository.searchCustomers(
                franchiseId, status,
                filterByUserIds ? userIds : null,
                filterByUserIds,
                pageable
        );
        return buildPageResponse(page);
    }

    @Override
    public CustomerFranchiseResponse getCustomerById(UUID id) {
        CustomerFranchise cf = customerFranchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        CustomerFranchiseResponse response = customerFranchiseMapper.toCustomerFranchiseResponse(cf);

        UserResponse user = identityClient.getUserById(cf.getUserId());
        response.setUserResponse(user);

        return response;
    }

    // ================== CREATE / SYNC ==================

    @Override
    @Transactional
    public void syncCustomerFromIdentity(UUID userId, CustomerType type) {
        CustomerFranchise cf = CustomerFranchise.builder()
                .userId(userId)
                .franchiseId(null)
                .type(type != null ? type : CustomerType.REGISTERED)
                .status(CustomerStatus.ACTIVE)
                .build();
        customerFranchiseRepository.save(cf);
    }

    @Override
    @Transactional
    public CustomerFranchise createCustomerAtFranchise(UUID userId, UUID franchiseId, CustomerType type) {
        // Sửa lại tên hàm trong repository cho khớp với thuộc tính userId
        if (customerFranchiseRepository.existsByUserIdAndFranchiseId(userId, franchiseId)) {
            throw new AppException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
        }

        CustomerFranchise cf = CustomerFranchise.builder()
                .userId(userId)
                .franchiseId(franchiseId)
                .type(type != null ? type : CustomerType.WALK_IN)
                .status(CustomerStatus.ACTIVE)
                .build();

        return customerFranchiseRepository.save(cf);
    }

    // ================== UPDATE ==================

    @Override
    @Transactional
    public CustomerFranchise updateCustomerFranchise(UUID id, UpdateCustomerRequest request) {
        CustomerFranchise customer = customerFranchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (request.getStatus() != null) customer.setStatus(request.getStatus());
        return customerFranchiseRepository.save(customer);
    }

    @Override
    @Transactional
    public CustomerFranchise updateCustomerStatus(UUID id, CustomerStatus status) {
        CustomerFranchise customer = customerFranchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        customer.setStatus(status);
        return customerFranchiseRepository.save(customer);
    }

    // ================== SOFT DELETE ==================

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        CustomerFranchise customer = customerFranchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        customer.setStatus(CustomerStatus.DELETED);
        customerFranchiseRepository.save(customer);
    }

    // ================== HELPER METHOD ==================

    private PageResponse<CustomerFranchiseResponse> buildPageResponse(Page<CustomerFranchise> pageResult) {
        if (pageResult.isEmpty()) {
            return PageResponse.<CustomerFranchiseResponse>builder()
                    .items(Collections.emptyList())
                    .currentPage(pageResult.getNumber())
                    .totalPages(pageResult.getTotalPages())
                    .totalItems(pageResult.getTotalElements())
                    .build();
        }

        List<UUID> userIds = pageResult.getContent().stream()
                .map(CustomerFranchise::getUserId)
                .distinct()
                .toList();

        List<UserResponse> users = identityClient.getUsersByIds(userIds);
        Map<UUID, UserResponse> userMap = users.stream()
                .collect(Collectors.toMap(UserResponse::getId, user -> user));

        List<CustomerFranchiseResponse> responses = pageResult.getContent().stream()
                .map(cf -> {
                    CustomerFranchiseResponse response = customerFranchiseMapper.toCustomerFranchiseResponse(cf);
                    response.setUserResponse(userMap.get(cf.getUserId()));

                    return response;
                })
                .toList();

        return PageResponse.<CustomerFranchiseResponse>builder()
                .items(responses)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }
}