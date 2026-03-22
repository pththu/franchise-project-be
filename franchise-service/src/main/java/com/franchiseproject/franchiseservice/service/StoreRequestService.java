package com.franchiseproject.franchiseservice.service;

import com.franchiseproject.franchiseservice.dto.StoreRequestDTO;
import com.franchiseproject.franchiseservice.enums.RequestStatus;

import java.util.List;
import java.util.UUID;

public interface StoreRequestService {
    // Admin endpoints
    List<StoreRequestDTO> getAllRequests();
    StoreRequestDTO getRequestById(UUID id);
    StoreRequestDTO getRequestByCode(String requestCode);
    List<StoreRequestDTO> getRequestsByFranchise(UUID franchiseId);
    List<StoreRequestDTO> getRequestsByStatus(RequestStatus status);
    StoreRequestDTO reviewRequest(UUID id, RequestStatus status, String adminNotes, Integer reviewedBy);
    StoreRequestDTO completeRequest(UUID id);  // New method for completing requests

    // Store manager endpoints (role handled by FE)
    StoreRequestDTO createRequest(StoreRequestDTO requestDTO);
    List<StoreRequestDTO> getRequestsByCreator(UUID createdBy);
    List<StoreRequestDTO> getRequestsByCreatorAndStatus(UUID createdBy, RequestStatus status);
}