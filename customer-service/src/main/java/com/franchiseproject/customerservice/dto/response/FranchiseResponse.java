package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.FranchiseStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseResponse {
//    UUID id;
//    String name;
//    String address;
//    String phone;
    UUID id;  // Changed from Long to UUID
    String name;
    String address;
    String googleMapsUrl;
    String phone;
    String email;
    LocalDate opened;
    LocalDate closed;
    String at;
    FranchiseStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}