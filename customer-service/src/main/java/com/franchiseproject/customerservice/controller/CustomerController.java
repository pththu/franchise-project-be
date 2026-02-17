package com.franchiseproject.customerservice.controller;

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

    // 1. API Search & Filter
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Customer> pageResult = customerService.searchCustomers(keyword, status, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // 2. API xem chi tiet Customer + Loyalty info
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerDetail(@PathVariable UUID id) {
        // Gọi Service lấy dữ liệu thật từ DB
        Customer customer = customerService.getCustomerById(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        // Lấy info Loyalty
        List<CustomerFranchise> loyaltyInfos = customerFranchiseService.getLoyaltyInfoByCustomerId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        response.put("loyalty", loyaltyInfos);

        return ResponseEntity.ok(response);
    }
}
