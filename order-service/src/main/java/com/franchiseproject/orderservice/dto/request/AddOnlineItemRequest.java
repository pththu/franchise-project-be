package com.franchiseproject.orderservice.dto.request;

import jakarta.validation.constraints.Min;
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
public class AddOnlineItemRequest {
    @NotNull(message = "Mã người dùng không được để trống")
    UUID customerId;

    @NotNull(message = "Mã sản phẩm không được để trống")
    UUID productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    Integer quantity;
}
