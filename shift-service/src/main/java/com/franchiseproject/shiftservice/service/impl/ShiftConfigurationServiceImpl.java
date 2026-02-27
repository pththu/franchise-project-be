package com.franchiseproject.shiftservice.service.impl;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import com.franchiseproject.shiftservice.mapper.ShiftMapper;
import com.franchiseproject.shiftservice.mapper.StaffShiftMapper;
import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import com.franchiseproject.shiftservice.enums.ShiftStatus;
import com.franchiseproject.shiftservice.model.StaffShift;
import com.franchiseproject.shiftservice.repository.ShiftConfigurationRepository;
import com.franchiseproject.shiftservice.repository.StaffShiftRepository;
import com.franchiseproject.shiftservice.service.ShiftConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftConfigurationServiceImpl implements ShiftConfigurationService {

    private final ShiftConfigurationRepository shiftRepository;
    private final StaffShiftRepository staffShiftRepository;
    private final ShiftMapper shiftMapper;
    private final StaffShiftMapper staffShiftMapper;

    @Override
    public ShiftResponse getShift(UUID id) {
        ShiftConfiguration shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        return shiftMapper.toResponse(shift);
    }

    @Override
        public UUID createShiftConfiguration(CreateShiftRequest request) {

        ShiftConfiguration shift = shiftMapper.toEntity(request);

        shift.setId(UUID.randomUUID());
        shift.setStatus(true);

        shiftRepository.save(shift);

        return shift.getId();
    }

    @Override
    public List<ShiftResponse> getShiftConfigurationsByFranchise(UUID franchiseId) {

        return shiftRepository.findByFranchiseId(franchiseId)
                .stream()
                .map(shiftMapper::toResponse)
                .toList();
    }

    @Override
    public UUID assignShift(AssignShiftRequest request) {

        if (staffShiftRepository.existsByStaffIdAndWorkDate(
                request.getStaffId(),
                request.getWorkDate())) {

            throw new IllegalArgumentException("Staff already has a shift on this date");
        }

        StaffShift staffShift = StaffShift.builder()
                .id(UUID.randomUUID())
                .staffId(request.getStaffId())
                .shiftConfigId(request.getShiftConfigId())
                .workDate(request.getWorkDate())
                .status(ShiftStatus.ASSIGNED)
                .build();

        staffShiftRepository.save(staffShift);

        return staffShift.getId();
    }

    @Override
    public StaffShiftResponse checkIn(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new RuntimeException("Cannot check in. Invalid shift status.");
        }

        staffShift.setCheckInTime(LocalTime.now());
        staffShift.setStatus(ShiftStatus.CHECKED_IN);

        staffShiftRepository.save(staffShift);

        return staffShiftMapper.toResponse(staffShift);
    }

    @Override
    public StaffShiftResponse checkOut(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (staffShift.getStatus() != ShiftStatus.CHECKED_IN) {
            throw new RuntimeException("Cannot check out before check in.");
        }

        staffShift.setCheckOutTime(LocalTime.now());
        staffShift.setStatus(ShiftStatus.CHECKED_OUT);

        staffShiftRepository.save(staffShift);
        return staffShiftMapper.toResponse(staffShift);
    }

    @Override
    public List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date) {

        return staffShiftRepository
                .findByStaffIdAndWorkDate(staffId, date)
                .stream()
                .map(staffShiftMapper::toResponse)
                .toList();
    }

    @Override
    public StaffShiftResponse markAbsent(UUID shiftId) {

        StaffShift staffShift = staffShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new RuntimeException("Cannot mark absent");
        }
        staffShift.setStatus(ShiftStatus.ABSENT);

        staffShiftRepository.save(staffShift);
        return staffShiftMapper.toResponse(staffShift);
    }
}