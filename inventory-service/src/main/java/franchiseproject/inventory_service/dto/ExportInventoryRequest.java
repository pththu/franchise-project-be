package franchiseproject.inventory_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

//dto chỉ dùng cho Export Inventory, Edit Export Record
@Getter
@Setter
public class ExportInventoryRequest {

    private UUID franchiseIngredientId;

    private Integer quantity;

    private UUID staffId;

}