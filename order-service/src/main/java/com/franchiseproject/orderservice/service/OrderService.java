package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> getAll();

    Order createOrder(CreateOrderRequest request);
    UUID checkoutOnline(CheckoutRequest request);
}
