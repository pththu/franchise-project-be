package com.franchiseproject.identityaccessservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsCountUserResponse {
    int totals;
    int totalIsActive;
    int totalIsDeleted;
    int totalIsSuspended;
    int totalAdmin;
    int totalManager;
    int totalStaff;
    int totalCustomer;
}
