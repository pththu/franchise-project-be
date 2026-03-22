package franchiseproject.inventory_service.entity;

import franchiseproject.inventory_service.enums.StockRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_requests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockRequest {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "request_code", nullable = false, unique = true, length = 50)
    String requestCode;

    @Column(name = "franchise_id", nullable = false)
    Long franchiseId; // TODO: Chuyển lại thành UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    StockRequestStatus status = StockRequestStatus.PENDING;

    @Column(name = "notes", length = 500)
    String notes;

    @Column(name = "created_by")
    UUID createdBy;

    @Column(name = "approved_by")
    UUID approvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @OneToMany(mappedBy = "stockRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockRequestItem> items = new ArrayList<>();
}
