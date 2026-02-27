package com.franchiseproject.shiftservice.controller;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.StaffShiftResponse;
import com.franchiseproject.shiftservice.service.ShiftConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftConfigurationService service;

    // 1. Tạo ca làm
    @PostMapping("/config")
    public UUID createShiftConfig(@RequestBody CreateShiftRequest request) {
        return service.createShiftConfiguration(request);
    }

    // 2. Lấy danh sách ca theo franchise
    @GetMapping("/config/{franchiseId}")
    public List<ShiftResponse> getShiftConfigs(@PathVariable UUID franchiseId) {
        return service.getShiftConfigurationsByFranchise(franchiseId);
    }

    // 3. Phân ca cho staff
    @PostMapping("/assign")
    public UUID assignShift(@RequestBody AssignShiftRequest request) {
        return service.assignShift(request);
    }

    // 4. Check-in (theo shiftId, không phải staffId)
    @PutMapping("/checkin/{shiftId}")
    public StaffShiftResponse checkIn(@PathVariable UUID shiftId) {
        return service.checkIn(shiftId);
    }

    // 5. Check-out
    @PutMapping("/checkout/{shiftId}")
    public StaffShiftResponse checkOut(@PathVariable UUID shiftId) {
        return service.checkOut(shiftId);
    }

    // 6. Xem lịch làm theo ngày
    @GetMapping("/schedule")
    public List<StaffShiftResponse> getSchedule(
            @RequestParam UUID staffId,
            @RequestParam LocalDate date
    ) {
        return service.getSchedule(staffId, date);
    }

    // 7. Báo vắng
    @PutMapping("/absent/{shiftId}")
    public StaffShiftResponse markAbsent(@PathVariable UUID shiftId) {
        return service.markAbsent(shiftId);
    }
}