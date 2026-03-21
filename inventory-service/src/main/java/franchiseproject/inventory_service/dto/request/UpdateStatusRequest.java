package franchiseproject.inventory_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Status cannot be null")
    Boolean isActive;

}