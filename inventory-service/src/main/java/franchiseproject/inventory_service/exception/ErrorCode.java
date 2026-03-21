package franchiseproject.inventory_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(400, "Invalid input data!"),
    INVALID_KEY(400, "Invalid key"),
    INVALID_CODE(400, "Invalid code verify"),
    INVALID_PASSWORD(400, "Password must be at least 8 characters"),
    INVALID_VERIFIED_CODE(400, "Invalid verification code"),
    CODE_EXPRIED(400, "Code has expired. Please request a new one"),
    DATA_IS_NULL(400, "Data is null"),
    IDENTIFIER_IS_REQUIRED(400, "Identifier is required"),
    UNAUTHORIZED(401, "Invalid identifier or password"),
    FORBIDDEN(403, "Access denied"),
    NOT_FOUND(404, "No Resource Found"),
    TOO_MANY_REQUESTS(429, "Too many request"),
    DUPLICATE_KEY(500, "Duplicate key value violates unique constraint"),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception"),
    ;
    private int code;
    private String message;
}
