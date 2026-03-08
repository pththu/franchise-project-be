package com.franchiseproject.paymentservice.mapper;


import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import org.mapstruct.Mapper;

@Mapper
public interface PaymentTransactionMapper {
    PaymentTransactionResponse toPaymentTransactionResponse(PaymentTransaction paymentTransaction);
}
