package com.franchiseproject.franchiseservice.dto;

import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FranchiseDTO {
    private Long id;
    private String name;
    private String address;
    private String googleMapsUrl;
    private String phone;
    private String email;
    private LocalDate opened;
    private LocalDate closed;
    private String at;
    private FranchiseStatus status; // Dùng enum
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}