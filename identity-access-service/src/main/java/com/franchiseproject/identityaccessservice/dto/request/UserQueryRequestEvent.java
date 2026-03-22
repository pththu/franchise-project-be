package com.franchiseproject.identityaccessservice.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserQueryRequestEvent {
    String correlationId;   // UUID để map request ↔ response
    List<UUID> userIds;     // danh sách userId cần lấy
    String replyTopic;
}
