package com.franchiseproject.paymentservice.mapper;

import com.franchiseproject.paymentservice.dto.response.PaymentMethodResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
    PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod);
}
