package com.franchiseproject.shiftservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    // 1. Tạo ca làm (tạo shift_config)
    @PostMapping("/config")
    public String createShiftConfig() {
        return "Create shift configuration";
    }

    // 2. Lấy danh sách ca theo franchise
    @GetMapping("/config/{franchiseId}")
    public String getShiftConfigs(@PathVariable UUID franchiseId) {
        return "Get shift configs of franchise " + franchiseId;
    }

    // 3. Phân ca cho staff (tạo staff_shift)
    @PostMapping("/assign")
    public String assignShift() {
        return "Assign shift to staff";
    }

    // 4. Check-in
    @PutMapping("/checkin/{shiftId}")
    public String checkIn(@PathVariable UUID shiftId) {
        return "Check-in shift " + shiftId;
    }

    // 5. Check-out
    @PutMapping("/checkout/{shiftId}")
    public String checkOut(@PathVariable UUID shiftId) {
        return "Check-out shift " + shiftId;
    }

    // 6. Xem lịch làm theo ngày
    @GetMapping("/schedule")
    public String getSchedule(
            @RequestParam UUID staffId,
            @RequestParam LocalDate date
    ) {
        return "Schedule of " + staffId + " at " + date;
    }

    // 7. Báo vắng
    @PutMapping("/absent/{shiftId}")
    public String markAbsent(@PathVariable UUID shiftId) {
        return "Absent shift " + shiftId;
    }
}
