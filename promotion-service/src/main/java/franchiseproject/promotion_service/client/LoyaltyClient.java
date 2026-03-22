package franchiseproject.promotion_service.client;

import franchiseproject.promotion_service.dto.CustomerTierResponse;
import franchiseproject.promotion_service.dto.UserLoyaltyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoyaltyClient {

    private final WebClient webClient;

    public String getRank(UUID userId, UUID franchiseId) {

        UserLoyaltyResponse<CustomerTierResponse> res = webClient.get()
                .uri("http://localhost:3005/api/loyalty/customers/{customerId}/franchises/{franchiseId}/tier-info",
                        userId, franchiseId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<UserLoyaltyResponse<CustomerTierResponse>>() {})
                .block();

        if (res == null || res.getData() == null) {
            return "BRONZE";
        }

        return res.getData().getCurrentTier(); // 🔥 đúng format mới
    }
}