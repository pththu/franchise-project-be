package franchiseproject.promotion_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import franchiseproject.promotion_service.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotion_scope")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionScope {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    @JsonBackReference
    Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    ScopeType scopeType;

    @Column(name = "scope_value", nullable = false)
    UUID scopeValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}