package com.franchiseproject.customerservice.controller;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.request.CreateCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SyncCustomerRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerDetailResponse;
import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.mapper.CustomerMapper;
import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.model.CustomerFranchise;
import com.franchiseproject.customerservice.service.CustomerFranchiseService;
import com.franchiseproject.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/customers")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerController {
    CustomerService customerService;
    CustomerFranchiseService customerFranchiseService;
    CustomerMapper customerMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCustomer() {
        Map<String, Object> response = new HashMap<>();

        List<Customer> customers = customerService.getAll();
        response.put("message", "Get All Customer");
        response.put("data", customers);

        return ResponseEntity.ok(response);
    }

    // Search and filter Customer
    @GetMapping("/search")
    public ApiResponse<PageResponse<CustomerResponse>> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<CustomerResponse>>builder()
                .statusCode(200)
                .message("Search customers successfully")
                .data(customerService.searchCustomers(keyword, status, pageable))
                .build();
    }

    // View Loyalty tier and point
    @GetMapping("/{id}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(@PathVariable UUID id) {
        Customer customer = customerService.getCustomerById(id);
        List<CustomerFranchise> loyaltyInfos = customerFranchiseService.getLoyaltyInfoByCustomerId(id);

        CustomerDetailResponse detailResponse = CustomerDetailResponse.builder()
                .customer(customerMapper.toCustomerResponse(customer))
                .loyaltyInfos(customerMapper.toLoyaltyInfoResponseList(loyaltyInfos))
                .build();

        return ApiResponse.<CustomerDetailResponse>builder()
                .statusCode(200)
                .message("Get customer detail successfully")
                .data(detailResponse)
                .build();
    }

    @GetMapping("/all/search")
    public ApiResponse<PageResponse<CustomerResponse>> searchAllCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<CustomerResponse>>builder()
                .statusCode(200)
                .message("Search all customers successfully")
                .data(customerService.searchAllCustomers(keyword, status, pageable))
                .build();
    }

    // Search (franchise)
    @GetMapping("/franchise/search")
    public ApiResponse<PageResponse<CustomerResponse>> searchCustomersByFranchise(
            // Ghi chú: franchiseId này thực tế nên lấy từ Context/JWT Token của nhân viên đăng nhập để bảo mật
            @RequestParam UUID franchiseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<CustomerResponse>>builder()
                .statusCode(200)
                .message("Search customers by franchise successfully")
                .data(customerService.searchCustomersByFranchise(franchiseId, keyword, status, pageable))
                .build();
    }

    // CREATE tại quầy
    @PostMapping("/franchise")
    public ApiResponse<CustomerResponse> createCustomerAtFranchise(
            @RequestParam UUID franchiseId, // Tương tự, ưu tiên lấy từ Token nhân viên
            @Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .statusCode(201)
                .message("Create or link customer to franchise successfully")
                .data(customerService.createOrLinkCustomerAtFranchise(request, franchiseId))
                .build();
    }

    // UPDATE khách hàng
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .statusCode(200)
                .message("Update customer successfully")
                .data(customerService.updateCustomer(id, request))
                .build();
    }

    // DELETE (Soft delete)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Delete customer successfully")
                .build();
    }

    // API NỘI BỘ: Để Identity Service gọi sang đồng bộ khi khách tự đăng ký App
    @PostMapping("/internal/sync")
    public ApiResponse<Void> syncCustomerFromIdentity(@RequestBody SyncCustomerRequest request) {
        customerService.syncCustomerFromIdentity(request);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Sync customer successfully")
                .build();
    }
}