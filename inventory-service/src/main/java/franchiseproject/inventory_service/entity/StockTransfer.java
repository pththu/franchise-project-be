package franchiseproject.inventory_service.entity;

import franchiseproject.inventory_service.enums.TransferStatus;
import franchiseproject.inventory_service.enums.TransferType;
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
@Table(name = "stock_transfers")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransfer {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "transfer_code", nullable = false, unique = true, length = 50)
    String transferCode;

    @Column(name = "from_location_id", nullable = false)
    Long fromLocationId; // TODO: Chuyển lại thành UUID

    @Column(name = "to_location_id", nullable = false)
    Long toLocationId; // TODO: Chuyển lại thành UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    TransferType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    TransferStatus status = TransferStatus.PENDING;

    @Column(name = "reference_request_id")
    UUID referenceRequestId;

    @Column(name = "notes", length = 500)
    String notes;

    @Column(name = "created_by")
    UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockTransferItem> items = new ArrayList<>();
}
