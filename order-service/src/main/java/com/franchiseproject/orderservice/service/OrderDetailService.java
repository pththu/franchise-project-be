package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.request.CreateOrderItemRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderItemRequest;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.entity.OrderDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderDetailService {
    List<OrderDetail> buildOrderDetails(
            List<CreateOrderItemRequest> items,
            Map<UUID, ProductResponse> apiProducts,
            Order order
    );

    BigDecimal calculateTotal(List<OrderDetail> details);

    Map<UUID, ProductResponse> fetchProducts(List<CreateOrderItemRequest> request);
    
    Map<UUID, ProductResponse> fetchProductsForUpdate(List<UpdateOrderItemRequest> request);
}
