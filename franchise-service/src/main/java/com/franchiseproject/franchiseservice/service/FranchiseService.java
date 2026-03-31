package com.franchiseproject.franchiseservice.service;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.dto.response.CheckFranchiseResponse;
import com.franchiseproject.franchiseservice.dto.response.FranchiseResponse;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.model.Franchise;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface FranchiseService {
    List<FranchiseDTO> getAllFranchises();
    FranchiseDTO getFranchiseById(UUID id);  // Đã sửa
    FranchiseDTO createFranchise(FranchiseDTO franchiseDTO);
    FranchiseDTO updateFranchise(UUID id, FranchiseDTO franchiseDTO);  // Đã sửa
    void deleteFranchise(UUID id);  // Đã sửa
    List<FranchiseDTO> getFranchisesByStatus(FranchiseStatus status);
    List<FranchiseDTO> getFranchisesByManagerId(Integer managerId);
    FranchiseDTO updateFranchiseStatus(UUID id, FranchiseStatus status);  // Đã sửa
    CheckFranchiseResponse checkFranchiseById(UUID id);
    List<FranchiseDTO> getFranchiseIsActive();
    List<FranchiseResponse> searchByIds(List<UUID> ids);
    List<FranchiseDTO> getFranchisesByIds(List<UUID> ids);
    Flux<ServerSentEvent<Object>> getFranchiseEvents();
}