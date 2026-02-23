package com.franchiseproject.identityaccessservice.exception;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // bat exception chung, chua xac dinh
//    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception) {
//        ApiResponse response = ApiResponse.builder()
//                .statusCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
//                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
//                .build();
//        return ResponseEntity.badRequest().body(response);
//    }

    // bat app exception
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse response = ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(exception.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // bat exception vi pham validation
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(errorKey);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        ApiResponse response = ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handlingNoResourceFoundException (NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .statusCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
}
