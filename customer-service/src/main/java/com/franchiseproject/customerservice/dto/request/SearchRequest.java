package com.franchiseproject.customerservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    String keyword; // username, fullname, email
    String status;
    UUID franchiseId;
    @Min(value = 0, message = "Page không được nhỏ hơn 0")
    Integer page = 0;
    @Min(value = 1, message = "Size tối thiểu là 1")
    @Max(value = 50, message = "Chỉ được lấy tối đa 50 user mỗi trang")
    Integer size = 10;
    String sortBy = "lastOrderAt";
    String sortDir = "desc";

    public Integer getPage() {
        return (this.page == null) ? 0 : this.page;
    }

    public Integer getSize() {
        return (this.size == null) ? 10 : this.size;
    }

    public String getSortBy() {
        return (this.sortBy == null || this.sortBy.isBlank()) ? "lastLogin" : this.sortBy;
    }

    public String getSortDir() {
        return (this.sortDir == null || this.sortDir.isBlank()) ? "desc" : this.sortDir;
    }
}
