package com.franchiseproject.shiftservice.service;

import com.franchiseproject.shiftservice.dto.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.ShiftResponse;
import com.franchiseproject.shiftservice.dto.StaffShiftResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShiftConfigurationService {

    ShiftResponse getShift(UUID id);

    UUID createShiftConfiguration(CreateShiftRequest request);

    List<ShiftResponse> getShiftConfigurationsByFranchise(UUID franchiseId);

    UUID assignShift(AssignShiftRequest request);

    StaffShiftResponse checkIn(UUID shiftId);

    StaffShiftResponse checkOut(UUID shiftId);

    List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date);

    StaffShiftResponse markAbsent(UUID shiftId);
}