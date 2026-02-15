package com.franchiseproject.shiftservice.controller;

import com.franchiseproject.shiftservice.dto.AssignShiftRequest;
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
    public UUID createShiftConfig() {
        return service.createShiftConfiguration();
    }

    // 2. Lấy danh sách ca theo franchise
    @GetMapping("/config/{franchiseId}")
    public List<UUID> getShiftConfigs(@PathVariable UUID franchiseId) {
        return service.getShiftConfigurationsByFranchise(franchiseId);
    }

    // 3. Phân ca cho staff
    @PostMapping("/assign")
    public UUID assignShift(@RequestBody AssignShiftRequest request) {
        return service.assignShift(
                request.getStaffId(),
                request.getShiftConfigId(),
                request.getWorkDate()
        );
    }

    // 4. Check-in
    @PutMapping("/checkin/{shiftId}")
    public void checkIn(@PathVariable UUID shiftId) {
        service.checkIn(shiftId);
    }

    // 5. Check-out
    @PutMapping("/checkout/{shiftId}")
    public void checkOut(@PathVariable UUID shiftId) {
        service.checkOut(shiftId);
    }

    // 6. Xem lịch làm theo ngày
    @GetMapping("/schedule")
    public List<UUID> getSchedule(
            @RequestParam UUID staffId,
            @RequestParam LocalDate date
    ) {
        return service.getSchedule(staffId, date);
    }

    // 7. Báo vắng
    @PutMapping("/absent/{shiftId}")
    public void markAbsent(@PathVariable UUID shiftId) {
        service.markAbsent(shiftId);
    }
}
