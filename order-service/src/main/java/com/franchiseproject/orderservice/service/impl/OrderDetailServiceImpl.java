package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.request.CreateOrderItemRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderItemRequest;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.client.ProductClient;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.entity.OrderDetail;
import com.franchiseproject.orderservice.repository.OrderDetailRepository;
import com.franchiseproject.orderservice.service.OrderDetailService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailServiceImpl implements OrderDetailService {
    OrderDetailRepository orderDetailRepository;
    ProductClient productClient;

    @Override
    @Transactional
    public List<OrderDetail> buildOrderDetails(List<CreateOrderItemRequest> items, Map<UUID, ProductResponse> apiProducts, Order order) {
        return items.stream()
                .map(item -> {
                    ProductResponse product = apiProducts.get(item.getProductId());
                    if (product == null) {
                        throw new AppException(ErrorCode.MISSING_PRODUCTS);
                    }
                    if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                        throw new AppException(ErrorCode.NOT_ENOUGH_QUANTITY_PRODUCT);
                    }
                    return OrderDetail.builder()
                            .productId(product.getId())
                            .productNameSnapshot(product.getName())
                            .priceSnapshot(product.getPrice())
                            .cost(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .quantity(item.getQuantity())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigDecimal calculateTotal(List<OrderDetail> details) {
        return details.stream()
                .map(d -> d.getPriceSnapshot()
                        .multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /// Tạo list productId để gửi sang product-service
    @Override
    @Transactional
    public Map<UUID, ProductResponse> fetchProducts(List<CreateOrderItemRequest> items) {
        List<UUID> productIds = items.stream()
                .map(CreateOrderItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());
        return productClient.getProductsByIds(productIds);
    }

    @Override
    @Transactional
    public Map<UUID, ProductResponse> fetchProductsForUpdate(List<UpdateOrderItemRequest> items) {
        List<UUID> productIds = items.stream()
                .map(UpdateOrderItemRequest::getProductId)
                .distinct()
                .toList();
        return productClient.getProductsByIds(productIds);
    }
}
