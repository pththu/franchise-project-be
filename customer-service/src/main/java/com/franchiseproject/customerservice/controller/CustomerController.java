package com.franchiseproject.customerservice.controller;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.response.CustomerDetailResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.model.CustomerFranchise;
import com.franchiseproject.customerservice.service.CustomerFranchiseService;
import com.franchiseproject.customerservice.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerController {
    CustomerService customerService;
    CustomerFranchiseService customerFranchiseService;

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
    public ApiResponse<PageResponse<Customer>> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<Customer>>builder()
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
                .customer(customer)
                .loyaltyInfos(loyaltyInfos)
                .build();

        return ApiResponse.<CustomerDetailResponse>builder()
                .statusCode(200)
                .message("Get customer detail successfully")
                .data(detailResponse)
                .build();
    }
}