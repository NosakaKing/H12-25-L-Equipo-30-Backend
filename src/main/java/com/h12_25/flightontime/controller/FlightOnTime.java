package com.h12_25.flightontime.controller;

import com.h12_25.flightontime.dto.FlightRequest;
import com.h12_25.flightontime.dto.PredictResponse;
import com.h12_25.flightontime.services.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class FlightOnTime {


    private final PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<PredictResponse> predict(@Valid @RequestBody  FlightRequest request) {
        // Llamamos al servicio que integra con el modelo de Data Science
        PredictResponse response = predictionService.getPredictionFastAPI(request);
        return ResponseEntity.ok(response);
    }
}
