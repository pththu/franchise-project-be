package franchiseproject.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UpdateFranchiseRequest {

    @NotBlank(message = "Franchise name cannot be empty")
    String name;

    @NotBlank(message = "Address cannot be empty")
    String address;

    Instant openedAt;

    Instant closedAt;

}