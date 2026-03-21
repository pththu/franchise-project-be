package franchiseproject.inventory_service.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    VALIDATION_FAILED(400, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(404, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    BAD_REQUEST(400, "Yêu cầu không hợp lệ hoặc dữ liệu không đủ", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
