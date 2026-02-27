package com.franchiseproject.shiftservice.service;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;

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