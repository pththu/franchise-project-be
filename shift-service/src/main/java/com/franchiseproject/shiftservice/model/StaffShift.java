package com.franchiseproject.shiftservice.model;

import com.franchiseproject.shiftservice.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "staff_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffShift {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID staffId;

    @Column(nullable = false)
    private UUID shiftConfigId;

    @Column(nullable = false)
    private LocalDate workDate;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;
}
