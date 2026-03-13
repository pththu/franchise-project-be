package com.franchiseproject.paymentservice.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    @NotNull(message = "orderId không được trống")
    UUID orderId;
    @NotNull(message = "customerId không được trống")
    UUID customerId;
    @Positive(message = "Tiền > 0")
    BigDecimal finalTotal;
    @NotNull(message = "orderStatus cần phải ở WAITING_PAYMENT mới có thể tiến thành thanh toán")
    String orderStatus;
}
