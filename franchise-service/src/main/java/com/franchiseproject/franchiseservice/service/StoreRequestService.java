package com.franchiseproject.franchiseservice.service;

import com.franchiseproject.franchiseservice.dto.StoreRequestDTO;
import com.franchiseproject.franchiseservice.enums.RequestStatus;

import java.util.List;

public interface StoreRequestService {
    // Admin endpoints
    List<StoreRequestDTO> getAllRequests();
    StoreRequestDTO getRequestById(Long id);
    StoreRequestDTO getRequestByCode(String requestCode);
    List<StoreRequestDTO> getRequestsByFranchise(Long franchiseId);
    List<StoreRequestDTO> getRequestsByStatus(RequestStatus status);
    StoreRequestDTO reviewRequest(Long id, RequestStatus status, String adminNotes, Integer reviewedBy);

    // Store manager endpoints
    StoreRequestDTO createRequest(StoreRequestDTO requestDTO);
    List<StoreRequestDTO> getRequestsByCustomer(Integer customerId);
    List<StoreRequestDTO> getRequestsByCustomerAndStatus(Integer customerId, RequestStatus status);
}