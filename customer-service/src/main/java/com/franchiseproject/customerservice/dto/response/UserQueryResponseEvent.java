package com.franchiseproject.customerservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserQueryResponseEvent {
    String correlationId;
    List<CustomerFranchiseResponse> customers;
}
