package com.h12_25.flightontime.client;

import com.h12_25.flightontime.dto.FlightRequest;
import com.h12_25.flightontime.dto.PredictResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DsClient {
    private final RestClient rest = RestClient.builder()
            .baseUrl("http://localhost:8000") // microservicio Python
            .build();

    public PredictResponse predict(FlightRequest req) {
        var entity = rest.post()
                .uri("/predict")
                .body(req)
                .retrieve()
                .toEntity(PredictResponse.class);
        return entity.getBody();
    }

}
