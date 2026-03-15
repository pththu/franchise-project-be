package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddAddressRequest {
    @NotNull(message = "Mã người dùng không được để trống")
    UUID customerId;

    @NotBlank(message = "Địa chỉ không được để trống")
    String address;
}

