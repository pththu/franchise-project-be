package com.franchiseproject.customerservice.dto.request;

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
public class SearchByRoleRequest {
    String roleName;
    @Min(value = 0, message = "Page không được nhỏ hơn 0")
    Integer page = 0;
    @Min(value = 1, message = "Size tối thiểu là 1")
    @Max(value = 50, message = "Chỉ được lấy tối đa 50 user mỗi trang")
    Integer size = 10;

    public Integer getPage() {
        return (this.page == null) ? 0 : this.page;
    }

    public Integer getSize() {
        return (this.size == null) ? 10 : this.size;
    }
}
