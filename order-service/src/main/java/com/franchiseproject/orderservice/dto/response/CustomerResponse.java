package com.franchiseproject.orderservice.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class CustomerResponse {
    UUID id;
    UUID userId;
    String fullName;
    String email;
    String phone;
}
