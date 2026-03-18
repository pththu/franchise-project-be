package com.franchiseproject.paymentservice.client;

import com.franchiseproject.paymentservice.dto.request.CreateMomoRequest;
import com.franchiseproject.paymentservice.dto.response.CreateMomoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MomoClient {
    private final RestClient momoRestClient;

    public CreateMomoResponse createMomoQR(CreateMomoRequest createMomoRequest) {
        return momoRestClient.post()
                .uri("/create")
                .body(createMomoRequest)
                .retrieve()
                .body(CreateMomoResponse.class);
    }
}
