package com.example.reportservice.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private UUID franchiseId;
    private UUID customerId;
    private UUID staffId;
    private UUID paymentTransactionId;
    private UUID promotionId;
    private String transactionReference;
    private String address;
    private BigDecimal totalDue;
    private String typeOrder;        // Dùng String thay vì Enum
    private String orderStatus;      // Dùng String thay vì Enum
    private BigDecimal priceShip;
    private Instant createAt;
    private Instant updateAt;
    private String customerName;
    private List<OrderItemResponse> orderDetails;
}