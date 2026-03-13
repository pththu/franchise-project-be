package com.franchiseproject.deliveryservice.dto.response;

import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import com.franchiseproject.deliveryservice.model.Delivery;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;


@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryHistoryResponse {
    UUID historyId;
    DeliverySatus status;
    String note;
    UUID updatedBy;
    Instant receivedAt;
    Instant updatedAt;
}
