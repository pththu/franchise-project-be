package com.franchiseproject.deliveryservice.repository;

import com.franchiseproject.deliveryservice.model.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, UUID> {
    List<DeliveryHistory> findByDelivery_DeliveryId(UUID deliveryId);
}
