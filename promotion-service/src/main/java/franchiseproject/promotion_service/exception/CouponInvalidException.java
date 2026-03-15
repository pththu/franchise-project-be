package franchiseproject.promotion_service.exception;

public class CouponInvalidException extends RuntimeException {
    public CouponInvalidException(String message) {
        super(message);
    }
}
