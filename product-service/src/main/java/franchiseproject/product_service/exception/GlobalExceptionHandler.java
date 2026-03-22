package franchiseproject.product_service.exception;

import franchiseproject.product_service.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
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
