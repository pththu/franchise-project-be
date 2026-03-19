package franchiseproject.product_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_KEY(400, "Invalid key"),
    DATA_IS_NULL(400, "Data is null"),
    INVALID_INPUT(400, "Invalid input data!"),
    INVALID_PRICE_RANGE(400, "toPrice must be greater than or equal to fromPrice"),
    PRODUCT_IS_DELETED(400, "Product deleted"),
    VARTIAN_IS_DELETED(400, "Variant deleted"),
    FORBIDDEN(403, "Access denied"),
    NOT_FOUND(404, "No Resource Found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    VARTIANT_NOT_FOUND(404, "Variant not found"),
    PRODUCT_VARTIANT_EXISTED(409, "Product variant is existed"),
    CATEGORY_EXISTED(409, "Category is existed"),
    PRODUCT_EXISTED(409, "Products is existed"),
    TOO_MANY_REQUESTS(429, "Too many request"),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception"),


    ;
    private int code;
    private String message;
}
