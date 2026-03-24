package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckResponse {
    boolean available; // true nếu locationId truyền lên có đủ hàng cho tất cả các món
    List<UUID> alternativeLocationIds; // chứa locationIds KHÁC có đủ ALL món
}
