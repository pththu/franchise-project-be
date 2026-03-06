package com.franchiseproject.shiftservice.service;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.PersonalStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShiftConfigurationService {

    ShiftResponse createShiftConfiguration(CreateShiftRequest request);

    ShiftResponse updateShiftConfiguration(UUID id, CreateShiftRequest request);

    ShiftResponse deleteShiftConfiguration(UUID id);

    List<ShiftResponse> getShiftConfigurationsByFranchise(UUID franchiseId);

    StaffShiftResponse assignShift(AssignShiftRequest request);

    StaffShiftResponse updateAssignedShift(UUID staffShiftId, AssignShiftRequest request);

    StaffShiftResponse checkIn(UUID shiftId);

    StaffShiftResponse checkOut(UUID shiftId);

    StaffShiftResponse markAbsent(UUID shiftId);

    List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date);

    ShiftStatisticResponse getStatisticByDate(LocalDate date);

    PersonalStatisticResponse getPersonalStatistic(UUID staffId);
}