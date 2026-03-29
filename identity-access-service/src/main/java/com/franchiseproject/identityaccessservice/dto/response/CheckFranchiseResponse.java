package com.franchiseproject.identityaccessservice.dto.response;

import com.franchiseproject.identityaccessservice.enums.FranchiseStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckFranchiseResponse {
    Boolean isExists;
    FranchiseStatus status;
}
