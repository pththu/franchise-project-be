package com.franchiseproject.paymentservice.repository;

import com.franchiseproject.paymentservice.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Optional<PaymentMethod> findByIdAndActiveTrue(UUID id);

    Optional<List<PaymentMethod>> findAllByActive(boolean active);
}
