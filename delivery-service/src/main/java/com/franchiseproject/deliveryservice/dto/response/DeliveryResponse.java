package com.franchiseproject.deliveryservice.dto.response;


import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.model.DeliveryHistory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryResponse {
    UUID deliveryId;
    UUID orderId;
    UUID staffId;
    double weight;
    Instant scheduledAt;
    DeliverySatus status;
    Instant createdAt;
    Instant updatedAt;
    List<DeliveryHistoryResponse> deliveryHistory;
}
