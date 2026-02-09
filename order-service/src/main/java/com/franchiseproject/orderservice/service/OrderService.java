package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.model.Order;

import java.util.List;

public interface OrderService {
    List<Order> getAll();
}
