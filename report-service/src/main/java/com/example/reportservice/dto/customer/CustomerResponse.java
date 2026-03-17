package com.example.reportservice.dto.customer;

import lombok.Data;
import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID id;
    private String customerCode;
    private String fullName;
    private String email;
    private String phone;
    private String membershipTier;
    private Integer loyaltyPoints;
}