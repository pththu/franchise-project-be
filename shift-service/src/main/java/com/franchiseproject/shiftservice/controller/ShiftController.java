package com.franchiseproject.shiftservice.controller;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.PersonalStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import com.franchiseproject.shiftservice.service.ShiftConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftConfigurationService service;

    // ================= SHIFT CONFIGURATION =================
    @PostMapping
    public ShiftResponse createShift(@RequestBody CreateShiftRequest request) {
        return service.createShiftConfiguration(request);
    }

    @GetMapping("/franchise/{franchiseId}")
    public List<ShiftResponse> getShiftsByFranchise(@PathVariable UUID franchiseId) {
        return service.getShiftConfigurationsByFranchise(franchiseId);
    }

    @PutMapping("/{id}")
    public ShiftResponse updateShift(
            @PathVariable UUID id,
            @RequestBody CreateShiftRequest request
    ) {
        return service.updateShiftConfiguration(id, request);
    }

    @DeleteMapping("/{id}")
    public ShiftResponse deleteShift(@PathVariable UUID id) {
        return service.deleteShiftConfiguration(id);
    }

    // ================= STAFF SHIFT (ASSIGNMENT) =================
    @PostMapping("/assignments")
    public StaffShiftResponse assignShift(@RequestBody AssignShiftRequest request) {
        return service.assignShift(request);
    }

    @PutMapping("/assignments/{assignmentId}")
    public StaffShiftResponse updateAssignment(
            @PathVariable UUID assignmentId,
            @RequestBody AssignShiftRequest request
    ) {
        return service.updateAssignedShift(assignmentId, request);
    }

    // ================= ATTENDANCE =================
    @PutMapping("/assignments/{id}/check-in")
    public StaffShiftResponse checkIn(@PathVariable UUID id) {
        return service.checkIn(id);
    }

    @PutMapping("/assignments/{id}/check-out")
    public StaffShiftResponse checkOut(@PathVariable UUID id) {
        return service.checkOut(id);
    }

    @PutMapping("/assignments/{id}/absent")
    public StaffShiftResponse markAbsent(@PathVariable UUID id) {
        return service.markAbsent(id);
    }

    // ================= SCHEDULE =================
    @GetMapping("/assignments")
    public List<StaffShiftResponse> getSchedule(
            @RequestParam(required = false) UUID staffId,
            @RequestParam LocalDate date
    ) {
        return service.getSchedule(staffId, date);
    }

    @GetMapping("/assignments/range")
    public List<StaffShiftResponse> getScheduleRange(
            @RequestParam UUID staffId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return service.getScheduleRange(staffId, startDate, endDate);
    }

    // ================= STATISTICS =================
    @GetMapping("/statistics")
    public ShiftStatisticResponse getStatisticByDate(
            @RequestParam LocalDate date
    ) {
        return service.getStatisticByDate(date);
    }

    @GetMapping("/statistics/{staffId}")
    public PersonalStatisticResponse getPersonalStatistic(
            @PathVariable UUID staffId
    ) {
        return service.getPersonalStatistic(staffId);
    }

    // ===== ATTENDANCE REPORT =====
    @GetMapping("/attendance/incomplete")
    public List<StaffShiftResponse> getIncompleteShifts(@RequestParam LocalDate date) {
        return service.getIncompleteShifts(date);
    }

    @GetMapping("/attendance/summary")
    public Map<String, Object> getAttendanceSummary(@RequestParam LocalDate date) {
        return service.getAttendanceSummary(date);
    }

    // ================= REAL-TIME SSE =================
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamShiftEvents() {
        return service.getShiftEvents();
    }
}