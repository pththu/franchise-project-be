package com.franchiseproject.customerservice.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveCustomerRequest {
    UUID franchiseId;
    UUID customerId;
}
