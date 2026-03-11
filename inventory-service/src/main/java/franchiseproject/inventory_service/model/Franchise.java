package franchiseproject.inventory_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "franchises")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Franchise {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "name")
    String name;

    @Column(name = "address")
    String address;

    @Column(name = "opened_at")
    Instant openedAt;

    @Column(name = "closed_at")
    Instant closedAt;

    @Column(name = "is_active")
    Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "franchise", cascade = CascadeType.ALL)
    List<FranchiseIngredient> franchiseIngredients;
}