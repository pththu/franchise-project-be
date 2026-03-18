package franchiseproject.promotion_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import franchiseproject.promotion_service.enums.DiscountType;
import franchiseproject.promotion_service.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    BigDecimal discountValue;

    @Column(name = "start_time")
    Instant startTime;

    @Column(name = "end_time")
    Instant endTime;

//    @Column(name = "coupon_code", unique = true, nullable = false)
//    String couponCode;
//
//    @Column(name = "coupon_usage_limit")
//    Integer usageLimit;
//
//    @Column(name = "coupon_used_count")
//    Integer usedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    PromotionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    @JsonManagedReference
    List<Coupon> coupons;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    @JsonManagedReference
    List<PromotionScope> scopes;

    @Column(name = "required_points")
    Integer requiredPoints;
}