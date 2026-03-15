package com.franchiseproject.orderservice.repository;

import com.franchiseproject.orderservice.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, UUID> {
}
