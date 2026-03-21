package franchiseproject.inventory_service.entity;

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
@Table(name = "inventory_import")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryImport {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    String code;

    @Column(name = "note", length = 500)
    String note;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    // who import
    @Column(name = "created_by")
    UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @OneToMany(mappedBy = "inventoryImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<InventoryImportItem> items = new ArrayList<>();
}