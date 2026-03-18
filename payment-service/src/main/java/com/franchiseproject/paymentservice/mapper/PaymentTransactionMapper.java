package com.franchiseproject.paymentservice.mapper;

import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses =  {PaymentMethodMapper.class})
public interface PaymentTransactionMapper {
    @Mapping(source = "paymentMethod", target = "paymentMethodResponse")
    PaymentTransactionResponse toPaymentTransactionResponse(PaymentTransaction paymentTransaction);
}
