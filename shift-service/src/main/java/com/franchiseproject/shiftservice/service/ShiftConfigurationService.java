package com.franchiseproject.shiftservice.service;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.PersonalStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import reactor.core.publisher.Flux;
import org.springframework.http.codec.ServerSentEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    List<StaffShiftResponse> getScheduleRange(UUID staffId, LocalDate startDate, LocalDate endDate);

    ShiftStatisticResponse getStatisticByDate(LocalDate date);

    PersonalStatisticResponse getPersonalStatistic(UUID staffId);

    List<StaffShiftResponse> getIncompleteShifts(LocalDate date);

    Map<String, Object> getAttendanceSummary(LocalDate date);

    // ================= SSE REAL-TIME =================
    Flux<ServerSentEvent<Object>> getShiftEvents();

    void emitShiftEvent(Object eventData);
}