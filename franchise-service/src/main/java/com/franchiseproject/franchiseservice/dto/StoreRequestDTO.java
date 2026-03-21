package com.franchiseproject.franchiseservice.dto;

import com.franchiseproject.franchiseservice.enums.RequestStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class StoreRequestDTO {
    private Long id;
    private String requestCode;
    private Long franchiseId;
    private String franchiseName;
    private String customerId;  // GIỮ NGUYÊN String
    private String customerName;
    private LocalDate requestDate;
    private Map<String, Object> requestData;
    private List<RequestItemDTO> items;
    private String notes;
    private BigDecimal totalAmount;
    private RequestStatus status;
    private String adminNotes;
    private Integer reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}