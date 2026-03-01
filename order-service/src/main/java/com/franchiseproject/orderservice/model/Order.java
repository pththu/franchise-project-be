package com.franchiseproject.orderservice.model;

import com.franchiseproject.orderservice.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.franchiseproject.orderservice.enums.TypeOrder;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "orders")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Order {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false)
    UUID id;
    @Column(name = "franchise_id", nullable = false)
    UUID franchiseId;
    @Column(name = "customer_id")
    UUID customerId;
    @Column(name = "staff_id")
    UUID staffId;
    @Column(name = "payment_transaction_id")
    UUID paymentTransactionId;
    @Column(name = "promotionId")
    UUID promotionId;
    @Column(name = "address")
    String address;
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
//    DeliveryAddress address;
    @Column(name = "total_due", precision = 12, scale = 2, nullable = false)
    BigDecimal totalDue;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    TypeOrder typeOrder;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    OrderStatus orderStatus;
    @Column(name = "price_ship", precision = 12, scale = 2)
    BigDecimal priceShip;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updateAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    List<OrderDetail> orderDetails;
    @Column(name = "assigned_staff_id")
    UUID assignedStaffId;
    @Column(name = "is_special")
    Boolean isSpecial;
    @Column(name = "estimated_delivery_time")
    Instant estimatedDeliveryTime;
}
