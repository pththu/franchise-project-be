package franchiseproject.inventory_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SetMinStockRequest {

    private UUID franchiseIngredientId;

    private Integer threshold;

    private UUID staffId;

}