package com.franchiseproject.franchiseservice.client;

import com.franchiseproject.franchiseservice.dto.CustomerInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:3003}")
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    CustomerInfoDTO getCustomerById(@PathVariable("id") Integer id);

    @GetMapping("/api/customers/{id}/validate")
    Boolean validateCustomer(@PathVariable("id") Integer id);
}