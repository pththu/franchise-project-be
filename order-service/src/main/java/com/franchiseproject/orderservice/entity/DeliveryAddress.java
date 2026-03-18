package com.franchiseproject.orderservice.entity;

import lombok.Data;

@Data
public class DeliveryAddress {
    String recipientName;
    String phone;
    String street;
    String ward;
    String district;
    String city;
}
