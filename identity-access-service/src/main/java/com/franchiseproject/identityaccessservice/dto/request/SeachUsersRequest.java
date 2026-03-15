package com.franchiseproject.identityaccessservice.dto.request;

import com.franchiseproject.identityaccessservice.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeachUsersRequest {
    String keyword;
    String role;
    String status;
    Boolean gender;
    @Min(value = 0, message = "Page không được nhỏ hơn 0")
    Integer page = 0;
    @Min(value = 1, message = "Size tối thiểu là 1")
    @Max(value = 50, message = "Chỉ được lấy tối đa 50 user mỗi trang")
    Integer size = 10;
    String sortBy = "lastLogin";
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
