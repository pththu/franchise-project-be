package com.franchiseproject.paymentservice.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(name = "order_id", nullable = false, unique = true)
    UUID orderId;
    @Column(name = "amount", nullable = false)
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status_transaction", nullable = false)
    StatusTransaction status;
    @Column(name = "transaction_ref", unique = true)
    String transactionRef;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    @JsonManagedReference
    PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "paymentTransaction")
    List<Refund> refunds;

    /// Flow đổi trạng thái của Transaction để theo một line không bị set ngược lại
    public void changeStatus(StatusTransaction newStatus) {

        if (this.status == StatusTransaction.SUCCESS) {
            return; // idempotent
        }
        if (this.status == StatusTransaction.CREATED && newStatus == StatusTransaction.PENDING) {
            this.status = StatusTransaction.PENDING;
            return;
        }
        if (this.status == StatusTransaction.PENDING && newStatus == StatusTransaction.SUCCESS) {
            this.status = StatusTransaction.SUCCESS;
            return;
        }
        if (this.status == StatusTransaction.PENDING && newStatus == StatusTransaction.FAILED) {
            this.status = StatusTransaction.FAILED;
            return;
        }
        if (this.status == StatusTransaction.PENDING && newStatus == StatusTransaction.CANCELLED) {
            this.status = StatusTransaction.CANCELLED;
            return;
        }
        if (this.status == StatusTransaction.PENDING && newStatus == StatusTransaction.EXPIRED) {
            this.status = StatusTransaction.EXPIRED;
            return;
        }
        throw new AppException(ErrorCode.INVALID_STATE_TRANSACTION);
    }
}
