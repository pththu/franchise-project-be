package com.franchiseproject.orderservice.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class CustomerResponse {
    UUID id;
    String fullName;
    String email;
    String phone;
}
