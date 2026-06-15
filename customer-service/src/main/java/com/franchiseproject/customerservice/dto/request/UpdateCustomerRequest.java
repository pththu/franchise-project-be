package com.franchiseproject.customerservice.dto.request;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCustomerRequest {
    String fullName;
    String phone;
    CustomerStatus status;
}