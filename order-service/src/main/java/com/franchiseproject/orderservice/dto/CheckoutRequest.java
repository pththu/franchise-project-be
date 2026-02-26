package com.franchiseproject.orderservice.dto;

import com.franchiseproject.orderservice.enums.TypeOrder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CheckoutRequest {
    private UUID franchiseId;
    private UUID customerId;
    private UUID staffId;
    private UUID paymentTransactionId;
    private UUID promotionId;

    private String address;
    private TypeOrder typeOrder;

    private List<OrderItemRequest> items;
}
