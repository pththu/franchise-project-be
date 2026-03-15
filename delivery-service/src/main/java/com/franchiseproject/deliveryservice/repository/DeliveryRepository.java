package com.franchiseproject.deliveryservice.repository;


import com.franchiseproject.deliveryservice.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Delivery findByOrderId(UUID orderId);
}

