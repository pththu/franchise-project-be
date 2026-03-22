package com.franchiseproject.franchiseservice.controller;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.service.FranchiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
public class FranchiseController {

    private final FranchiseService franchiseService;

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
}