package com.franchiseproject.orderservice.repository;

import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByCustomerId(UUID customerId);

    Page<Order> findByFranchiseId(UUID franchiseId, Pageable pageable);
    
    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.orderStatus = :status)
            AND (:typeOrder IS NULL OR o.typeOrder = :typeOrder)
            """)
    Page<Order> findByFilters(
            @Param("status") OrderStatus status,
            @Param("typeOrder") TypeOrder typeOrder,
            Pageable pageable
    );

    @Query("""
            SELECT o FROM Order o
            WHERE o.franchiseId = :franchiseId
            AND (:status IS NULL OR o.orderStatus = :status)
            AND (:typeOrder IS NULL OR o.typeOrder = :typeOrder)
            """)
    Page<Order> findByFranchiseIdAndFilters(
            @Param("franchiseId") UUID franchiseId,
            @Param("status") OrderStatus status,
            @Param("typeOrder") TypeOrder typeOrder,
            Pageable pageable
    );

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
                WHERE CAST(o.id AS text) LIKE %:keyword%
            """)
    List<Order> searchOrderByIdLike(@Param("keyword") String keyword);

    @Query("""
            SELECT o FROM Order o
            WHERE (CAST(o.id AS text) LIKE %:keyword% OR o.customerId IN :customerIds)
            """)
    List<Order> searchOrdersByCustomerIdsWithoutFranchise(@Param("keyword") String keyword, @Param("customerIds") java.util.List<java.util.UUID> customerIds);

    @Query("""
                SELECT o FROM Order o
                WHERE CAST(o.id AS text) LIKE %:keyword% OR o.customerId IN :customerIds
            """)
    List<Order> searchOrderByIdOrCustomerIds(@Param("keyword") String keyword, @Param("customerIds") java.util.List<java.util.UUID> customerIds);
    @Query("""
            SELECT o FROM Order o
            WHERE o.franchiseId = :franchiseId
            AND CAST(o.id AS text) LIKE %:keyword%
            """)
    List<Order> searchOrders(@Param("franchiseId") UUID franchiseId, @Param("keyword") String keyword);

    @Query("""
            SELECT o FROM Order o
            WHERE o.franchiseId = :franchiseId
            AND (CAST(o.id AS text) LIKE %:keyword% OR o.customerId IN :customerIds)
            """)
    List<Order> searchOrdersByCustomerIds(@Param("franchiseId") UUID franchiseId, @Param("keyword") String keyword, @Param("customerIds") java.util.List<java.util.UUID> customerIds);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndOrderStatus(
            UUID customerId,
            OrderStatus status,
            Pageable pageable
    );

    Optional<Order> findOrderById(UUID orderId);
}
