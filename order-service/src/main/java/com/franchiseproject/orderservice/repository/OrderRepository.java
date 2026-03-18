package com.franchiseproject.orderservice.repository;

import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByCustomerId(UUID customerId);

    Page<Order> findByFranchiseId(UUID franchiseId, Pageable pageable);

    Page<Order> findByFranchiseIdAndOrderStatus(
            UUID franchiseId,
            OrderStatus orderStatus,
            Pageable pageable
    );

    Page<Order> findByOrderStatus(
            OrderStatus status,
            Pageable pageable
    );

    @Query("""
                SELECT o FROM Order o
                WHERE CAST(o.id as string) LIKE %:keyword%
            """)
    List<Order> searchOrderByIdLike(String keyword);

    @Query("""
            SELECT o FROM Order o
            WHERE o.franchiseId = :franchiseId
            AND CAST(o.id AS string) LIKE %:keyword%
            """)
    List<Order> searchOrders(UUID franchiseId, String keyword);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndOrderStatus(
            UUID customerId,
            OrderStatus status,
            Pageable pageable
    );

    Optional<Order> findOrderById(UUID orderId);
}
