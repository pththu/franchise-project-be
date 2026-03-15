package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.entity.OrderStatusLog;
import java.util.List;

public interface OrderStatusLogService {
    List<OrderStatusLog> getAll();
}
