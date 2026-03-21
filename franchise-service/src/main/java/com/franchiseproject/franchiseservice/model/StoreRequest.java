package com.franchiseproject.franchiseservice.model;

import com.franchiseproject.franchiseservice.enums.RequestStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String requestCode;

    @ManyToOne
    @JoinColumn(name = "franchise_id", nullable = false)
    private Franchise franchise;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)  // Cách 1: Dùng JdbcTypeCode
    private String requestData;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    private LocalDateTime reviewedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}