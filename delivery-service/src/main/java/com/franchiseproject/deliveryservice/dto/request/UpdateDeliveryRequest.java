package com.franchiseproject.deliveryservice.dto.request;

import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.model.DeliveryHistory;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateDeliveryRequest {
    @NotNull(message = "Không để trống id quản lý")
    UUID staffId;
    UUID shipperId;
    DeliverySatus status;
    double weight;
    Instant scheduledAt;
    List<DeliveryHistory> deliveryHistory;
}
