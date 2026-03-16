package com.franchiseproject.customerservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCustomerRequest {
    @NotBlank(message = "Full name must not be blank")
    String fullName;

    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Phone must not be blank")
    String phone;
}