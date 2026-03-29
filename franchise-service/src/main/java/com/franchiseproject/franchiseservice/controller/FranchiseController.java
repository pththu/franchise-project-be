package com.franchiseproject.franchiseservice.controller;

import com.franchiseproject.franchiseservice.dto.ApiResponse;
import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.dto.response.CheckStatusFranchiseResponse;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.exception.AppException;
import com.franchiseproject.franchiseservice.exception.ErrorCode;
import com.franchiseproject.franchiseservice.mapper.FranchiseMapper;
import com.franchiseproject.franchiseservice.service.FranchiseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FranchiseController {

    FranchiseService franchiseService;
    FranchiseMapper franchiseMapper;

    @PostMapping
    public ResponseEntity<FranchiseDTO> createFranchise(@Valid @RequestBody FranchiseDTO franchiseDTO) {
        return new ResponseEntity<>(franchiseService.createFranchise(franchiseDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FranchiseDTO>> getAllFranchises() {
        return ResponseEntity.ok(franchiseService.getAllFranchises());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FranchiseDTO> getFranchiseById(@PathVariable("id") UUID id) {  // Đã sửa
        return ResponseEntity.ok(franchiseService.getFranchiseById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FranchiseDTO>> getFranchisesByStatus(@PathVariable("status") FranchiseStatus status) {
        return ResponseEntity.ok(franchiseService.getFranchisesByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FranchiseDTO> updateFranchise(
            @PathVariable("id") UUID id,  // Đã sửa
            @Valid @RequestBody FranchiseDTO franchiseDTO) {
        return ResponseEntity.ok(franchiseService.updateFranchise(id, franchiseDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFranchise(@PathVariable("id") UUID id) {  // Đã sửa
        franchiseService.deleteFranchise(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FranchiseDTO> updateFranchiseStatus(
            @PathVariable("id") UUID id,  // Đã sửa
            @RequestBody FranchiseStatus status) {
        return ResponseEntity.ok(franchiseService.updateFranchiseStatus(id, status));
    }

    // response chuan
    @GetMapping("/detail/{franchiseId}")
    public ApiResponse<FranchiseDTO> getById(@PathVariable("franchiseId") UUID id) {
        return ApiResponse.<FranchiseDTO>builder()
                .statusCode(200)
                .message("Get franchise by id: " + id)
                .data(franchiseService.getFranchiseById(id))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<FranchiseDTO>> getAll() {
        return ApiResponse.<List<FranchiseDTO>>builder()
                .statusCode(200)
                .message("Get all franchise")
                .data(franchiseService.getAllFranchises())
                .build();
    }

    @GetMapping("/check/{franchiseId}")
    public ApiResponse<CheckStatusFranchiseResponse> checkStatusFranchise(@PathVariable("franchiseId") UUID id) {
        return ApiResponse.<CheckStatusFranchiseResponse>builder()
                .statusCode(200)
                .message("Check status franchise: " + id)
                .data(franchiseService.checkFranchiseById(id))
                .build();
    }
}