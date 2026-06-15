package com.franchiseproject.franchiseservice.dto;

import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FranchiseDTO {
    private UUID id;  // Changed from Long to UUID
    private String name;
    private String address;
    private String googleMapsUrl;
    private String phone;
    private String email;
    private LocalDate opened;
    private LocalDate closed;
    private String at;
    private FranchiseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}