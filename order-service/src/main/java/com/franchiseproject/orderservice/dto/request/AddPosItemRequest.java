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
public class AddPosItemRequest {
    @NotBlank(message = "Mã thiết bị không được để trống")
    String terminalId;

    @NotNull(message = "Mã sản phẩm không được để trống")
    UUID productId;

    @NotNull(message = "Mã biến thể không được để trống")
    UUID variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    Integer quantity;
}
