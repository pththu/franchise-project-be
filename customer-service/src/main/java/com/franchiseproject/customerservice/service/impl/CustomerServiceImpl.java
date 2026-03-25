package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {

    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

    @Override
    public CustomerFranchise getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    // ================== CREATE / SYNC ==================
    @Override
    @Transactional
    public void syncCustomerFromIdentity(UUID customerId, CustomerType type) {
        // User tự đăng ký App -> franchiseId = null
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

    // ================== SEARCH & FILTER ==================
    @Override
    public PageResponse<CustomerFranchise> searchCustomers(UUID franchiseId, CustomerStatus status, List<UUID> customerIds, Pageable pageable) {
        boolean filterByCustomerIds = customerIds != null && !customerIds.isEmpty();

        // Nếu Frontend truyền keyword và Identity trả về rỗng -> Tức là không có user nào khớp -> Trả về mảng rỗng
        if (customerIds != null && customerIds.isEmpty()) {
            return buildPageResponse(Page.empty(pageable));
        }

        Page<CustomerFranchise> page = customerRepository.searchCustomers(
                franchiseId, status,
                filterByCustomerIds ? customerIds : null,
                filterByCustomerIds,
                pageable
        );
        return buildPageResponse(page);
    }

    // ================== CREATE / LINK ==================
    @Override
    @Transactional
    public CustomerFranchise createCustomerAtFranchise(UUID customerId, UUID franchiseId, CustomerType type) {
        if (customerRepository.existsByCustomerIdAndFranchiseId(customerId, franchiseId)) {
            throw new AppException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
        }

        CustomerFranchise cf = CustomerFranchise.builder()
                .customerId(customerId) // ID từ identity
                .franchiseId(franchiseId) // ID chi nhánh của Staff tạo
                .type(type != null ? type : CustomerType.WALK_IN)
                .status(CustomerStatus.ACTIVE)
                .loyaltyTier(LoyaltyTier.BRONZE)
                .loyaltyCurrentPoint(0)
                .loyaltyTotalPoint(0)
                .build();

        return customerRepository.save(cf);
    }

    // ================== UPDATE ==================
    @Override
    @Transactional
    public CustomerFranchise updateCustomerFranchise(UUID id, UpdateCustomerRequest request) {
        CustomerFranchise customer = getCustomerById(id);

        if (request.getStatus() != null) customer.setStatus(request.getStatus());
        // Có thể update CustomerType nếu sau này có nhu cầu

        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public CustomerFranchise updateCustomerStatus(UUID id, CustomerStatus status) {
        CustomerFranchise customer = getCustomerById(id);
        customer.setStatus(status);
        return customerRepository.save(customer);
    }

    // ================== SOFT DELETE ==================
    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        CustomerFranchise customer = getCustomerById(id);
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

//    @Override
//    public PageResponse<CustomerFranchise> getCustomersForAdmin(CustomerStatus status, Pageable pageable) {
//        Page<CustomerFranchise> page = (status == null)
//                ? customerRepository.findAll(pageable)
//                : customerRepository.findByStatus(status, pageable);
//        return buildPageResponse(page);
//    }
//
//    @Override
//    public PageResponse<CustomerFranchise> getCustomersForManager(UUID franchiseId, CustomerStatus status, Pageable pageable) {
//        Page<CustomerFranchise> page = (status == null)
//                ? customerRepository.findByFranchiseId(franchiseId, pageable)
//                : customerRepository.findByFranchiseIdAndStatus(franchiseId, status, pageable);
//        return buildPageResponse(page);
//    }

    private PageResponse<CustomerFranchise> buildPageResponse(Page<CustomerFranchise> pageResult) {
        return PageResponse.<CustomerFranchise>builder()
                .items(pageResult.getContent())
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }
}