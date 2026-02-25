package com.franchiseproject.loyaltyservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // Ẩn field data nếu nó bị null
public class ApiResponse<T> {
    int statusCode;
    String message;
    T data;
}