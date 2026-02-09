package com.franchiseproject.orderservice.model;

import lombok.*;
import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_details")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetail {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    @Column(name = "product_id", nullable = false)
    UUID productId;
    @Column(name = "product_name_snapshot", nullable = false)
    String productNameSnapshot;
    @Column(name = "quantity", nullable = false)
    Integer quantity;
    @Column(name = "price_snapshot", precision = 12, scale = 2, nullable = false)
    BigDecimal priceSnapshot;
    @Column(name = "cost", precision = 12, scale = 2, nullable = false)
    BigDecimal cost;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "UUID")
    Order order;
}
