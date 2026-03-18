package com.franchiseproject.paymentservice.enums;



public enum StatusTransaction {
    CREATED,
    PENDING,
    SUCCESS,  //Thành công
    FAILED, //Thất bại
    CANCELLED,
    EXPIRED,
    REFUNDED
}
