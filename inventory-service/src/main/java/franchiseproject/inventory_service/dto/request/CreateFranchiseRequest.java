package franchiseproject.inventory_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateFranchiseRequest {

    @NotBlank(message = "Franchise name cannot be empty")
    String name;

    @NotBlank(message = "Address cannot be empty")
    String address;

    @NotNull(message = "Opened time cannot be null")
    Instant openedAt;

    Instant closedAt;

}