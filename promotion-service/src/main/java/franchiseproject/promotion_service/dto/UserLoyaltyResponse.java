package franchiseproject.promotion_service.dto;

import lombok.Data;

@Data
public class UserLoyaltyResponse<T> {

    private int statusCode;
    private String message;
    private T data;
}