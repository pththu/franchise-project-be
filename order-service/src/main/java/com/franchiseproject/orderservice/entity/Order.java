package com.franchiseproject.orderservice.entity;

import com.franchiseproject.orderservice.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.franchiseproject.orderservice.enums.TypeOrder;
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
    @Column(name = "address")
    String address;
    @Column(name = "total_discount", precision = 12, scale = 2)
    BigDecimal totalDiscount; //tổng số tiền đã trừ
    @Column(name = "total_due", precision = 12, scale = 2, nullable = false)
    BigDecimal totalDue; //tổng số tiền phải trả
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
    @Column(name = "estimated_delivery_time")
    Instant estimatedDeliveryTime;
}
