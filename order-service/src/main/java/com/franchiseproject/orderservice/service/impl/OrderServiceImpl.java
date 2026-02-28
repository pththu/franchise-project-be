    package com.franchiseproject.orderservice.service.impl;

    import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
    import com.franchiseproject.orderservice.dto.request.UpdateOrderItemRequest;
    import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
    import com.franchiseproject.orderservice.dto.response.OrderResponse;
    import com.franchiseproject.orderservice.enums.OrderStatus;
    import com.franchiseproject.orderservice.exception.AppException;
    import com.franchiseproject.orderservice.exception.ErrorCode;
    import com.franchiseproject.orderservice.mapper.OrderMapper;
    import com.franchiseproject.orderservice.model.Order;
    import com.franchiseproject.orderservice.model.OrderDetail;
    import com.franchiseproject.orderservice.model.OrderStatusLog;
    import com.franchiseproject.orderservice.repository.OrderRepository;
    import com.franchiseproject.orderservice.repository.OrderStatusLogRepository;
    import com.franchiseproject.orderservice.service.OrderService;
    import jakarta.transaction.Transactional;
    import lombok.AccessLevel;
    import lombok.AllArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;

    @Service
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class OrderServiceImpl implements OrderService {
        private final OrderStatusLogRepository orderStatusLogRepository;
        OrderRepository orderRepository;
        OrderMapper  orderMapper;
        RedisTemplate<String, Object> redisTemplate;
        @Override
        public List<OrderResponse> getAll() {
            return orderRepository.findAll()
                    .stream()
                    .map(orderMapper::toOrderResponse)
                    .toList();
        }

        @Override
        @Transactional
        public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            if (order.getOrderStatus() == OrderStatus.COMPLETED
                    || order.getOrderStatus() == OrderStatus.CANCELED
                    || order.getOrderStatus() == OrderStatus.REFUNDED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
            }
            order.setOrderStatus(newStatus);
            orderRepository.save(order);
        }

        @Override
        public List<OrderResponse> getOrderByCustomerId(UUID customerId) {
           List<Order> o =  orderRepository.findAllByCustomerId(customerId);
           return o.stream().map(orderMapper::toOrderResponse).toList();
        }

        @Override
        @Transactional
        public Order updateOrder(UUID orderId, UpdateOrderRequest request) {

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            // Kiểm tra trạng thái order có thể cập nhật không
            OrderStatus currentStatus = order.getOrderStatus();
            if (currentStatus == OrderStatus.COMPLETED ||
                    currentStatus == OrderStatus.CANCELED ||
                    currentStatus == OrderStatus.FAILED ||
                    currentStatus == OrderStatus.REFUNDED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
            }

            // Validate items
            if (request.getItems() == null || request.getItems().isEmpty()) {

            }

            // Validate shipping price
            if (request.getPriceShip() == null || request.getPriceShip().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Shipping price must be >= 0");
            }

            // Cập nhật thông tin order
            if (request.getStaffId() != null) {
                order.setStaffId(request.getStaffId());
            }
            if (request.getPaymentTransactionId() != null) {
                order.setPaymentTransactionId(request.getPaymentTransactionId());
            }
            if (request.getPromotionId() != null) {
                order.setPromotionId(request.getPromotionId());
            }
            if (request.getAddress() != null) {
                order.setAddress(request.getAddress());
            }
            if (request.getTypeOrder() != null) {
                order.setTypeOrder(request.getTypeOrder());
            }

            order.setPriceShip(request.getPriceShip());

            // Xóa các order details cũ
            order.getOrderDetails().clear();

            // Tạo order details mới
            BigDecimal totalItems = BigDecimal.ZERO;
            List<OrderDetail> newOrderDetails = new ArrayList<>();

            for (UpdateOrderItemRequest item : request.getItems()) {

                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0");
                }

                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Price must be >= 0");
                }

                BigDecimal itemTotal = item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                totalItems = totalItems.add(itemTotal);

                OrderDetail detail = OrderDetail.builder()
                        .productId(item.getProductId())
                        .productNameSnapshot(item.getProductName())
                        .priceSnapshot(item.getPrice())
                        .cost(item.getCost())
                        .quantity(item.getQuantity())
                        .order(order)
                        .build();

                newOrderDetails.add(detail);
            }

            // Cập nhật tổng tiền
            BigDecimal finalTotal = totalItems.add(request.getPriceShip());
            order.setTotalDue(finalTotal);
            order.setOrderDetails(newOrderDetails);

            // Lưu order đã cập nhật
            Order updatedOrder = orderRepository.save(order);

            // Log việc cập nhật order
            OrderStatusLog log = OrderStatusLog.builder()
                    .statusId(UUID.randomUUID())
                    .formStatus(currentStatus.name())
                    .toStatus(currentStatus.name())
                    .noteLog("Order updated - total due changed to " + finalTotal)
                    .ordrer(updatedOrder)
                    .build();

            orderStatusLogRepository.save(log);

            return updatedOrder;
        }

        @Override
        public void addAddressOnlineOrder(AddAddressRequest request) {
            String key = "online_order:" + request.getCustomerId();
            redisTemplate.opsForValue().set(key, request.getAddress());
        }

        @Override
        public String getAddressOnlineOrder(UUID customerId) {
            String key = "online_order:" + customerId;
            Object address = redisTemplate.opsForValue().get(key);
            return address != null ? address.toString() : null;
        }
    }
