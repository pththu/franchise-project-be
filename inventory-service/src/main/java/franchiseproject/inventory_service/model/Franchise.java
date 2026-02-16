package franchiseproject.inventory_service.model;
import lombok.*;
import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "franchises")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Franchise {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true,nullable = false)
UUID id;
@Column(name = "name")
String name;
    @Column(name = "address")
String address;
    @Column(name = "opened_at")
Instant openedAt;
    @Column(name = "close_at")
Instant closedAt;
    @Column(name = "is_active")
Boolean isActive;
    @Column(name = "create_at")
    @CreationTimestamp
Instant createAt;
    @Column(name = "update_at")
    @UpdateTimestamp
Instant updateAt;
    @OneToMany(mappedBy = "franchise", cascade = CascadeType.ALL)
    List<FranchiseIngredient> franchiseIngredients;
}
