package com.franchiseproject.franchiseservice.service.impl;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.dto.response.CheckFranchiseResponse;
import com.franchiseproject.franchiseservice.dto.response.FranchiseResponse;
import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import com.franchiseproject.franchiseservice.exception.AppException;
import com.franchiseproject.franchiseservice.exception.BadRequestException;
import com.franchiseproject.franchiseservice.exception.ErrorCode;
import com.franchiseproject.franchiseservice.exception.ResourceNotFoundException;
import com.franchiseproject.franchiseservice.mapper.FranchiseMapper;
import com.franchiseproject.franchiseservice.model.Franchise;
import com.franchiseproject.franchiseservice.repository.FranchiseRepository;
import com.franchiseproject.franchiseservice.service.FranchiseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import org.springframework.http.codec.ServerSentEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FranchiseServiceImpl implements FranchiseService {

    FranchiseRepository franchiseRepository;
    FranchiseMapper franchiseMapper;

    // ================= SSE SINK =================
    private final Sinks.Many<Object> franchiseSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public List<FranchiseDTO> getAllFranchises() {
        return franchiseRepository.findAll()
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FranchiseDTO getFranchiseById(UUID id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));
        return franchiseMapper.toDTO(franchise);
    }

    @Override
    @Transactional
    public FranchiseDTO createFranchise(FranchiseDTO franchiseDTO) {
        if (franchiseRepository.existsByName(franchiseDTO.getName())) {
            throw new BadRequestException("Franchise name already exists: " + franchiseDTO.getName());
        }

        Franchise franchise = franchiseMapper.toEntity(franchiseDTO);

        if (franchiseDTO.getStatus() == null) {
            franchise.setStatus(FranchiseStatus.NEW);
        }

        Franchise savedFranchise = franchiseRepository.save(franchise);
        log.info("New franchise created: {} with id {}", savedFranchise.getName(), savedFranchise.getId());

        // Push realtime
        emitFranchiseEvent(Map.of("type", "FRANCHISE_CREATED", "data", franchiseMapper.toDTO(savedFranchise)));

        return franchiseMapper.toDTO(savedFranchise);
    }

    @Override
    @Transactional
    public FranchiseDTO updateFranchise(UUID id, FranchiseDTO franchiseDTO) {
        Franchise existingFranchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        if (!existingFranchise.getName().equals(franchiseDTO.getName())
                && franchiseRepository.existsByName(franchiseDTO.getName())) {
            throw new BadRequestException("Franchise name already exists: " + franchiseDTO.getName());
        }

        existingFranchise.setName(franchiseDTO.getName());
        existingFranchise.setAddress(franchiseDTO.getAddress());
        existingFranchise.setGoogleMapsUrl(franchiseDTO.getGoogleMapsUrl());
        existingFranchise.setPhone(franchiseDTO.getPhone());
        existingFranchise.setEmail(franchiseDTO.getEmail());
        existingFranchise.setOpened(franchiseDTO.getOpened());
        existingFranchise.setClosed(franchiseDTO.getClosed());
        existingFranchise.setAt(franchiseDTO.getAt());

        if (franchiseDTO.getStatus() != null) {
            existingFranchise.setStatus(franchiseDTO.getStatus());
        }

        Franchise updatedFranchise = franchiseRepository.save(existingFranchise);
        log.info("Franchise updated: {} with id {}", updatedFranchise.getName(), id);

        emitFranchiseEvent(Map.of("type", "FRANCHISE_UPDATED", "data", franchiseMapper.toDTO(updatedFranchise)));

        return franchiseMapper.toDTO(updatedFranchise);
    }

    @Override
    @Transactional
    public void deleteFranchise(UUID id) {
        Franchise existingFranchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        existingFranchise.setStatus(FranchiseStatus.DELETED);
        franchiseRepository.save(existingFranchise);
        log.info("Franchise deleted with id: {}", id);

        emitFranchiseEvent(Map.of("type", "FRANCHISE_DELETED", "franchiseId", id));
    }

    @Override
    public List<FranchiseDTO> getFranchisesByStatus(FranchiseStatus status) {
        return franchiseRepository.findByStatus(status)
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FranchiseDTO> getFranchisesByManagerId(Integer managerId) {
        return franchiseRepository.findAll()
                .stream()
                .map(franchiseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FranchiseDTO updateFranchiseStatus(UUID id, FranchiseStatus status) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + id));

        franchise.setStatus(status);
        Franchise updatedFranchise = franchiseRepository.save(franchise);
        log.info("Franchise {} status updated to: {}", id, status);

        emitFranchiseEvent(Map.of("type", "FRANCHISE_STATUS_UPDATED", "franchiseId", id, "status", status));

        return franchiseMapper.toDTO(updatedFranchise);
    }

    @Override
    public CheckFranchiseResponse checkFranchiseById(UUID id) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return CheckFranchiseResponse.builder()
                .isExists(franchise != null)
                .status(franchise != null ? franchise.getStatus() : null)
                .build();
    }

    @Override
    public List<FranchiseDTO> getFranchiseIsActive() {
        return franchiseRepository.findByStatus(FranchiseStatus.ACTIVE)
                .stream().map(franchiseMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<FranchiseResponse> searchByIds(List<UUID> ids) {
        return franchiseRepository.findByIds(ids)
                .stream()
                .map(franchiseMapper::toFranchiseResponse)
                .toList();
    }

    public List<FranchiseDTO> getFranchisesByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return franchiseRepository.findAllById(ids).stream()
                .map(franchiseMapper::toDTO)
                .toList();
    }

    // ================= SSE METHODS =================
    @Override
    public Flux<ServerSentEvent<Object>> getFranchiseEvents() {
        return franchiseSink.asFlux()
                .map(event -> ServerSentEvent.<Object>builder()
                        .id(UUID.randomUUID().toString())
                        .event("franchise-update")
                        .data(event)
                        .build());
    }

    public void emitFranchiseEvent(Object eventData) {
        franchiseSink.tryEmitNext(eventData);
    }
}