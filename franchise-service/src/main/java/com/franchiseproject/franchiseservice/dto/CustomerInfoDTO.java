package com.franchiseproject.franchiseservice.dto;

import lombok.Data;

@Data
public class CustomerInfoDTO {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
}