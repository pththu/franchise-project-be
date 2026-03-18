package com.franchiseproject.paymentservice.mapper;

import com.franchiseproject.paymentservice.dto.response.PaymentMethodResponse;
import com.franchiseproject.paymentservice.entity.PaymentMethod;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
    List<PaymentMethodResponse> toPaymentMethodResponse(List<PaymentMethod> paymentMethod);
}
