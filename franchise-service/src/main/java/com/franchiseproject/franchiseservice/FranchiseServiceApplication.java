package com.franchiseproject.franchiseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.franchiseproject.franchiseservice.client")
public class FranchiseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FranchiseServiceApplication.class, args);
    }
}