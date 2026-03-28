package com.franchiseproject.franchiseservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_KEY(400, "Invalid key"),
    DATA_IS_NULL(400, "Data is null"),
    INVALID_INPUT(400, "Invalid input data!"),
    FRANCHISE_IS_DELETED(400, "Product deleted"),
    NOT_FOUND(404, "No Resource Found"),
    FRANCHISE_NOT_FOUND(404, "Franchise not found"),
    FRANCHISE_EXISTED(409, "Franchise is existed"),
    TOO_MANY_REQUESTS(429, "Too many request"),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception"),

    ;

    private int code;
    private String message;
}
