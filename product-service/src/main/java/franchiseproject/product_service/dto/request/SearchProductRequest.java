package franchiseproject.product_service.dto.request;

import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchProductRequest {
    String keyword;
    String categoryName;
    String status;
    String color;
    String size;
    @Min(value = 0, message = "Giá tiền nhỏ nhất là 0")
    BigDecimal fromPrice;
    @Min(value = 0, message = "Giá tiền nhỏ nhất là 0")
    BigDecimal toPrice;
    Integer page = 0;
    @Min(value = 1, message = "Size tối thiểu là 1")
    @Max(value = 50, message = "Chỉ được lấy tối đa 30 products mỗi trang")
    Integer sizePage = 30;
    String sortBy = "name";
    String sortDir = "desc";

    public Integer getPage() {
        return (this.page == null) ? 0 : this.page;
    }

    public Integer getSizePage() {
        return (this.sizePage == null) ? 10 : this.sizePage;
    }

    public String getSortBy() {
        return (this.sortBy == null || this.sortBy.isBlank()) ? "name" : this.sortBy;
    }

    public String getSortDir() {
        return (this.sortDir == null || this.sortDir.isBlank()) ? "desc" : this.sortDir;
    }
}
