package com.franchiseproject.franchiseservice.service;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;

import java.util.List;

public interface FranchiseService {
    // CRUD operations
    List<FranchiseDTO> getAllFranchises();
    FranchiseDTO getFranchiseById(Long id);
    FranchiseDTO createFranchise(FranchiseDTO franchiseDTO);
    FranchiseDTO updateFranchise(Long id, FranchiseDTO franchiseDTO);
    void deleteFranchise(Long id);

    // Additional operations
    List<FranchiseDTO> getFranchisesByStatus(FranchiseStatus status);
    List<FranchiseDTO> getFranchisesByManagerId(Integer managerId);
    FranchiseDTO updateFranchiseStatus(Long id, FranchiseStatus status);
}