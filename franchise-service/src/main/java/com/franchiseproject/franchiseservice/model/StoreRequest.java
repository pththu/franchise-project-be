package com.franchiseproject.franchiseservice.model;

import com.franchiseproject.franchiseservice.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "request_code", unique = true, nullable = false, length = 50)
    private String requestCode;

    @ManyToOne
    @JoinColumn(name = "franchise_id", nullable = false)
    private Franchise franchise;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String requestData;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}