package com.franchiseproject.customerservice.controller;

import com.franchiseproject.customerservice.model.Customer;
import com.franchiseproject.customerservice.service.CustomerFranchiseService;
import com.franchiseproject.customerservice.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
