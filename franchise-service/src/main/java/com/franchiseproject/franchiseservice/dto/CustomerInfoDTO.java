package com.franchiseproject.franchiseservice.dto;

import lombok.Data;

@Data
public class CustomerInfoDTO {
    private String id;  // GIỮ NGUYÊN String (UUID từ customer-service vẫn là string)
    private String fullName;
    private String email;
    private String phone;
    private String status;
}