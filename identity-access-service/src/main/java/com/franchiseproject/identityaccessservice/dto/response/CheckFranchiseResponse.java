package com.franchiseproject.identityaccessservice.dto.response;

import com.franchiseproject.identityaccessservice.enums.FranchiseStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckFranchiseResponse {
    Boolean isExists;
    FranchiseStatus status;
}
