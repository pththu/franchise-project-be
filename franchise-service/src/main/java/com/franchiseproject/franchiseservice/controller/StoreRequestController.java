package com.franchiseproject.franchiseservice.controller;

import com.franchiseproject.franchiseservice.dto.StoreRequestDTO;
import com.franchiseproject.franchiseservice.enums.RequestStatus;
import com.franchiseproject.franchiseservice.service.StoreRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class StoreRequestController {

    private final StoreRequestService storeRequestService;

    @PostMapping
    public ResponseEntity<StoreRequestDTO> createRequest(@Valid @RequestBody StoreRequestDTO requestDTO) {
        return new ResponseEntity<>(storeRequestService.createRequest(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/my-requests/{createdBy}")
    public ResponseEntity<List<StoreRequestDTO>> getMyRequests(@PathVariable("createdBy") UUID createdBy) {
        return ResponseEntity.ok(storeRequestService.getRequestsByCreator(createdBy));
    }

    @GetMapping("/my-requests/{createdBy}/status/{status}")
    public ResponseEntity<List<StoreRequestDTO>> getMyRequestsByStatus(
            @PathVariable("createdBy") UUID createdBy,
            @PathVariable("status") RequestStatus status) {
        return ResponseEntity.ok(storeRequestService.getRequestsByCreatorAndStatus(createdBy, status));
    }

    @GetMapping
    public ResponseEntity<List<StoreRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(storeRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreRequestDTO> getRequestById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(storeRequestService.getRequestById(id));
    }

    @GetMapping("/code/{requestCode}")
    public ResponseEntity<StoreRequestDTO> getRequestByCode(@PathVariable("requestCode") String requestCode) {
        return ResponseEntity.ok(storeRequestService.getRequestByCode(requestCode));
    }

    @GetMapping("/franchise/{franchiseId}")
    public ResponseEntity<List<StoreRequestDTO>> getRequestsByFranchise(@PathVariable("franchiseId") UUID franchiseId) {
        return ResponseEntity.ok(storeRequestService.getRequestsByFranchise(franchiseId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<StoreRequestDTO>> getRequestsByStatus(@PathVariable("status") RequestStatus status) {
        return ResponseEntity.ok(storeRequestService.getRequestsByStatus(status));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<StoreRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(storeRequestService.getRequestsByStatus(RequestStatus.PENDING));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<StoreRequestDTO> reviewRequest(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> reviewData) {

        RequestStatus status = RequestStatus.valueOf((String) reviewData.get("status"));
        String adminNotes = (String) reviewData.get("adminNotes");
        Integer reviewedBy = (Integer) reviewData.get("reviewedBy");

        return ResponseEntity.ok(storeRequestService.reviewRequest(id, status, adminNotes, reviewedBy));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<StoreRequestDTO> completeRequest(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(storeRequestService.completeRequest(id));
    }
}