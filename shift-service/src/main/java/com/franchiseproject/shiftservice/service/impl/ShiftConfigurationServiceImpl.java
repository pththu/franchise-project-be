package com.franchiseproject.shiftservice.service.impl;

import com.franchiseproject.shiftservice.dto.request.AssignShiftRequest;
import com.franchiseproject.shiftservice.dto.request.CreateShiftRequest;
import com.franchiseproject.shiftservice.dto.response.PersonalStatisticResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftResponse;
import com.franchiseproject.shiftservice.dto.response.ShiftStatisticResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import org.springframework.http.codec.ServerSentEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShiftConfigurationServiceImpl implements ShiftConfigurationService {

    private final ShiftConfigurationRepository shiftRepository;
    private final StaffShiftRepository staffShiftRepository;
    private final ShiftMapper shiftMapper;
    private final StaffShiftMapper staffShiftMapper;

    // ================= SSE SINK =================
    private final Sinks.Many<Object> shiftSink = Sinks.many().multicast().onBackpressureBuffer();

    // Constants
    private static final int GRACE_PERIOD_MINUTES = 15;
    private static final int LATE_THRESHOLD_MINUTES = 30;
    private static final int CHECKIN_TIMEOUT_MINUTES = 30;
    private static final int CHECKOUT_TIMEOUT_MINUTES = 30;
    private static final int MAX_SHIFTS_PER_WEEK = 6;

    // ================= SHIFT CONFIG =================
    @Override
    public ShiftResponse createShiftConfiguration(CreateShiftRequest request) {
        validateShiftConfig(request);

        log.info("Creating shift: {}", request);
        ShiftConfiguration shift = shiftMapper.toEntity(request);
        shift.setId(UUID.randomUUID());
        shift.setStatus(true);

        ShiftResponse response = shiftMapper.toResponse(shiftRepository.save(shift));
        emitShiftEvent(Map.of("type", "SHIFT_CREATED", "data", response));
        log.info("Shift created: {}", response);
        return response;
    }

    @Override
    public ShiftResponse updateShiftConfiguration(UUID id, CreateShiftRequest request) {
        validateShiftConfig(request);

        ShiftConfiguration shift = getShiftConfigOrThrow(id);
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setFranchiseId(request.getFranchiseId());

        ShiftResponse response = shiftMapper.toResponse(shiftRepository.save(shift));
        emitShiftEvent(Map.of("type", "SHIFT_UPDATED", "data", response));
        return response;
    }

    @Override
    public ShiftResponse deleteShiftConfiguration(UUID id) {
        ShiftConfiguration shift = getShiftConfigOrThrow(id);
        shiftRepository.delete(shift);
        emitShiftEvent(Map.of("type", "SHIFT_DELETED", "shiftId", id));
        return shiftMapper.toResponse(shift);
    }

    @Override
    public List<ShiftResponse> getShiftConfigurationsByFranchise(UUID franchiseId) {
        return shiftRepository.findByFranchiseId(franchiseId)
                .stream()
                .map(shiftMapper::toResponse)
                .toList();
    }

    // ================= ASSIGN SHIFT =================
    @Override
    public StaffShiftResponse assignShift(AssignShiftRequest request) {
        validateAssignRequest(request);

        StaffShift staffShift = StaffShift.builder()
                .id(UUID.randomUUID())
                .staffId(request.getStaffId())
                .shiftConfigId(request.getShiftConfigId())
                .workDate(request.getWorkDate())
                .status(ShiftStatus.ASSIGNED)
                .build();

        StaffShiftResponse response = staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
        emitShiftEvent(Map.of("type", "ASSIGN_SHIFT", "assignmentId", response.getId(), "data", response));
        return response;
    }

    @Override
    public StaffShiftResponse updateAssignedShift(UUID staffShiftId, AssignShiftRequest request) {
        StaffShift staffShift = getStaffShiftOrThrow(staffShiftId);
        boolean hasChanges = false;

        if (request.getWorkDate() != null && !request.getWorkDate().equals(staffShift.getWorkDate())) {
            validateWorkDateChange(staffShift, request.getWorkDate());
            staffShift.setWorkDate(request.getWorkDate());
            hasChanges = true;
        }

        if (request.getShiftConfigId() != null && !request.getShiftConfigId().equals(staffShift.getShiftConfigId())) {
            validateShiftConfigChange(request.getShiftConfigId());
            staffShift.setShiftConfigId(request.getShiftConfigId());
            hasChanges = true;
        }

        if (!hasChanges) {
            return staffShiftMapper.toResponse(staffShift);
        }

        StaffShiftResponse response = staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
        emitShiftEvent(Map.of("type", "UPDATE_ASSIGNMENT", "assignmentId", staffShiftId, "data", response));
        return response;
    }

    // ================= ATTENDANCE =================
    @Override
    public StaffShiftResponse checkIn(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);
        validateCheckIn(staffShift);

        LocalTime now = LocalTime.now();
        ShiftConfiguration config = getShiftConfigOrThrow(staffShift.getShiftConfigId());
        LocalTime startTime = config.getStartTime();

        if (now.isBefore(startTime)) {
            throw new IllegalStateException(
                    String.format("Chưa đến giờ check-in. Giờ bắt đầu: %s, hiện tại: %s", startTime, now)
            );
        }

        long lateMinutes = Duration.between(startTime, now).toMinutes();

        if (lateMinutes > GRACE_PERIOD_MINUTES) {
            staffShift.setLateMinutes((int) lateMinutes);
            staffShift.setNote("Check-in trễ " + lateMinutes + " phút");
        }

        staffShift.setCheckInTime(now);
        staffShift.setStatus(ShiftStatus.CHECKED_IN);

        StaffShiftResponse response = staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
        emitShiftEvent(Map.of("type", "CHECK_IN", "assignmentId", shiftId, "staffId", staffShift.getStaffId(), "data", response));
        return response;
    }

    @Override
    public StaffShiftResponse checkOut(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);
        validateCheckOut(staffShift);

        ShiftConfiguration config = getShiftConfigOrThrow(staffShift.getShiftConfigId());
        LocalTime now = LocalTime.now();
        LocalTime endTime = config.getEndTime();

        if (now.isAfter(endTime.plusMinutes(CHECKOUT_TIMEOUT_MINUTES))) {
            staffShift.setStatus(ShiftStatus.INCOMPLETE);
            staffShift.setNote("Quên check-out - xử lý sau " + CHECKOUT_TIMEOUT_MINUTES + " phút");
        } else {
            staffShift.setStatus(ShiftStatus.CHECKED_OUT);
        }

        staffShift.setCheckOutTime(now);

        StaffShiftResponse response = staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
        emitShiftEvent(Map.of("type", "CHECK_OUT", "assignmentId", shiftId, "staffId", staffShift.getStaffId(), "data", response));
        return response;
    }

    @Override
    public StaffShiftResponse markAbsent(UUID shiftId) {
        StaffShift staffShift = getStaffShiftOrThrow(shiftId);

        if (staffShift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException("Không thể đánh dấu vắng mặt cho ca đã xử lý");
        }

        if (staffShift.getWorkDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Không thể đánh dấu vắng mặt cho ca trong tương lai");
        }

        staffShift.setStatus(ShiftStatus.ABSENT);
        staffShift.setNote("Vắng mặt");

        StaffShiftResponse response = staffShiftMapper.toResponse(staffShiftRepository.save(staffShift));
        emitShiftEvent(Map.of("type", "MARK_ABSENT", "assignmentId", shiftId, "staffId", staffShift.getStaffId(), "data", response));
        return response;
    }

    @Override
    public List<StaffShiftResponse> getSchedule(UUID staffId, LocalDate date) {
        try {
            List<StaffShift> shifts = (staffId != null)
                    ? staffShiftRepository.findByStaffIdAndWorkDate(staffId, date)
                    : staffShiftRepository.findByWorkDate(date);

            log.info("Found {} shifts for date {} (staffId: {})",
                    shifts.size(), date, staffId);

            return shifts.stream()
                    .map(shift -> {
                        StaffShiftResponse response = staffShiftMapper.toResponse(shift);
                        enhanceWithShiftDetails(response, shift);
                        return response;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error getting schedule for date {}: {}", date, e.getMessage());
            throw e;
        }
    }

    @Override
    public ShiftStatisticResponse getStatisticByDate(LocalDate date) {
        List<StaffShift> shifts = staffShiftRepository.findByWorkDate(date);

        return ShiftStatisticResponse.builder()
                .totalShifts(shifts.size())
                .totalAssigned(countByStatus(shifts, ShiftStatus.ASSIGNED))
                .totalCheckedIn(countByStatus(shifts, ShiftStatus.CHECKED_IN))
                .totalCheckedOut(countByStatus(shifts, ShiftStatus.CHECKED_OUT))
                .totalAbsent(countByStatus(shifts, ShiftStatus.ABSENT))
                .totalIncomplete(countByStatus(shifts, ShiftStatus.INCOMPLETE))
                .build();
    }

    @Override
    public PersonalStatisticResponse getPersonalStatistic(UUID staffId) {
        List<StaffShift> shifts = staffShiftRepository.findByStaffId(staffId);

        return PersonalStatisticResponse.builder()
                .totalShifts(shifts.size())
                .totalCompleted(countByStatus(shifts, ShiftStatus.CHECKED_OUT))
                .totalAbsent(countByStatus(shifts, ShiftStatus.ABSENT))
                .totalLate(countLateShifts(shifts))
                .totalIncomplete(countByStatus(shifts, ShiftStatus.INCOMPLETE))
                .build();
    }

    @Override
    public List<StaffShiftResponse> getIncompleteShifts(LocalDate date) {
        return staffShiftRepository.findByWorkDateAndStatus(date, ShiftStatus.INCOMPLETE)
                .stream()
                .map(staffShiftMapper::toResponse)
                .toList();
    }

    @Override
    public Map<String, Object> getAttendanceSummary(LocalDate date) {
        List<StaffShift> shifts = staffShiftRepository.findByWorkDate(date);

        return Map.of(
                "date", date.toString(),
                "totalShifts", shifts.size(),
                "checkedOut", countByStatus(shifts, ShiftStatus.CHECKED_OUT),
                "checkedIn", countByStatus(shifts, ShiftStatus.CHECKED_IN),
                "assigned", countByStatus(shifts, ShiftStatus.ASSIGNED),
                "absent", countByStatus(shifts, ShiftStatus.ABSENT),
                "incomplete", countByStatus(shifts, ShiftStatus.INCOMPLETE),
                "lateList", shifts.stream()
                        .filter(s -> s.getLateMinutes() != null && s.getLateMinutes() > 0)
                        .map(s -> Map.of(
                                "staffId", s.getStaffId().toString(),
                                "lateMinutes", s.getLateMinutes(),
                                "note", s.getNote() != null ? s.getNote() : ""
                        ))
                        .toList()
        );
    }

    // ================= SSE METHODS =================
    @Override
    public Flux<ServerSentEvent<Object>> getShiftEvents() {
        return shiftSink.asFlux()
                .map(event -> ServerSentEvent.<Object>builder()
                        .id(UUID.randomUUID().toString())
                        .event("shift-update")
                        .data(event)
                        .build());
    }

    @Override
    public void emitShiftEvent(Object eventData) {
        shiftSink.tryEmitNext(eventData);
    }

    // ================= SCHEDULED JOBS =================
    @Scheduled(cron = "0 */5 * * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processMissedCheckIns() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Running missed check-in job at {}", now);

        List<StaffShift> assignedShifts = staffShiftRepository
                .findByWorkDateAndStatus(today, ShiftStatus.ASSIGNED);

        for (StaffShift shift : assignedShifts) {
            try {
                ShiftConfiguration config = getShiftConfigOrThrow(shift.getShiftConfigId());
                LocalTime startTime = config.getStartTime();

                if (now.isAfter(startTime.plusMinutes(CHECKIN_TIMEOUT_MINUTES))) {
                    shift.setStatus(ShiftStatus.ABSENT);
                    shift.setNote("Vắng mặt - không check-in sau " + CHECKIN_TIMEOUT_MINUTES + " phút");
                    staffShiftRepository.save(shift);
                    log.info("Marked shift {} as ABSENT (missed check-in after {} mins)", shift.getId(), CHECKIN_TIMEOUT_MINUTES);
                }
            } catch (Exception e) {
                log.error("Failed to process shift {}: {}", shift.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processMissedCheckOuts() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Running missed check-out job at {}", now);

        List<StaffShift> activeShifts = staffShiftRepository
                .findByWorkDateAndStatus(today, ShiftStatus.CHECKED_IN);

        for (StaffShift shift : activeShifts) {
            try {
                ShiftConfiguration config = getShiftConfigOrThrow(shift.getShiftConfigId());
                LocalTime endTime = config.getEndTime();

                if (now.isAfter(endTime.plusMinutes(CHECKOUT_TIMEOUT_MINUTES))) {
                    shift.setStatus(ShiftStatus.INCOMPLETE);
                    shift.setCheckOutTime(endTime);
                    shift.setNote("Quên check-out - tự động xử lý sau " + CHECKOUT_TIMEOUT_MINUTES + " phút");
                    staffShiftRepository.save(shift);
                    log.info("Marked shift {} as INCOMPLETE (missed check-out after {} mins)", shift.getId(), CHECKOUT_TIMEOUT_MINUTES);
                }
            } catch (Exception e) {
                log.error("Failed to process shift {}: {}", shift.getId(), e.getMessage());
            }
        }
    }

    // ================= PRIVATE HELPERS =================
    private void validateShiftConfig(CreateShiftRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc");
        }
    }

    private void validateAssignRequest(AssignShiftRequest request) {
        ShiftConfiguration shiftConfig = getShiftConfigOrThrow(request.getShiftConfigId());

        if (!shiftConfig.getStatus()) {
            throw new IllegalStateException("Ca làm việc đã bị vô hiệu hóa");
        }

        if (request.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể phân ca trong quá khứ");
        }

        if (staffShiftRepository.existsByStaffIdAndWorkDate(
                request.getStaffId(), request.getWorkDate())) {
            throw new IllegalArgumentException("Nhân viên đã có ca làm việc trong ngày này");
        }

        checkWeeklyShiftLimit(request.getStaffId(), request.getWorkDate());
    }

    private void validateCheckIn(StaffShift shift) {
        if (shift.getStatus() != ShiftStatus.ASSIGNED) {
            throw new IllegalStateException(
                    String.format("Không thể check-in. Trạng thái hiện tại: %s", shift.getStatus())
            );
        }

        LocalDate today = LocalDate.now();
        if (!shift.getWorkDate().equals(today)) {
            throw new IllegalStateException(
                    String.format("Chỉ được check-in trong ngày làm việc. Ngày ca: %s, hôm nay: %s",
                            shift.getWorkDate(), today)
            );
        }
    }

    private void validateCheckOut(StaffShift shift) {
        if (shift.getStatus() != ShiftStatus.CHECKED_IN) {
            throw new IllegalStateException("Chưa check-in hoặc đã check-out");
        }
    }

    private void validateWorkDateChange(StaffShift shift, LocalDate newDate) {
        if (staffShiftRepository.existsByStaffIdAndWorkDateAndIdNot(
                shift.getStaffId(), newDate, shift.getId())) {
            throw new IllegalArgumentException("Nhân viên đã có ca khác trong ngày này");
        }
    }

    private void validateShiftConfigChange(UUID shiftConfigId) {
        ShiftConfiguration newConfig = shiftRepository.findById(shiftConfigId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc"));

        if (!newConfig.getStatus()) {
            throw new IllegalStateException("Ca làm việc đã bị vô hiệu hóa");
        }
    }

    private ShiftConfiguration getShiftConfigOrThrow(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc"));
    }

    private StaffShift getStaffShiftOrThrow(UUID id) {
        return staffShiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca đã phân công"));
    }

    private void checkWeeklyShiftLimit(UUID staffId, LocalDate workDate) {
        LocalDate weekStart = workDate.minusDays(workDate.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        long weeklyShifts = staffShiftRepository.countByStaffIdAndWorkDateBetweenAndStatusIn(
                staffId,
                weekStart,
                weekEnd,
                List.of(ShiftStatus.ASSIGNED, ShiftStatus.CHECKED_IN, ShiftStatus.CHECKED_OUT)
        );

        if (weeklyShifts >= MAX_SHIFTS_PER_WEEK) {
            throw new IllegalStateException(
                    String.format("Nhân viên đã đạt giới hạn %d ca/tuần", MAX_SHIFTS_PER_WEEK)
            );
        }
    }

    private long countByStatus(List<StaffShift> shifts, ShiftStatus status) {
        return shifts.stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }

    private long countLateShifts(List<StaffShift> shifts) {
        return shifts.stream()
                .filter(s -> s.getLateMinutes() != null && s.getLateMinutes() > 0)
                .count();
    }

    private void enhanceWithShiftDetails(StaffShiftResponse response, StaffShift shift) {
        try {
            shiftRepository.findById(shift.getShiftConfigId())
                    .ifPresent(config -> {
                        response.setShiftName(config.getName());
                        response.setShiftStartTime(config.getStartTime());
                        response.setShiftEndTime(config.getEndTime());
                    });
        } catch (Exception e) {
            log.warn("Could not enhance shift {} with details: {}", shift.getId(), e.getMessage());
        }
    }
}