package com.franchiseproject.paymentservice.repository;

import com.franchiseproject.paymentservice.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymenMethodRepository extends JpaRepository<PaymentMethod, UUID> {
}
