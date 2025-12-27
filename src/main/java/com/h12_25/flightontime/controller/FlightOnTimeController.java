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

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FlightOnTimeController {


    private final PredictionService predictionService;

    /**
     * Endpoint REST para realizar predicciones de vuelos.
     *
     * - @PostMapping("/predict") → expone la ruta /predict para recibir solicitudes POST.
     * - @Valid @RequestBody FlightRequest request → recibe en el cuerpo de la petición un objeto JSON
     *   con los datos del vuelo, validado automáticamente según las anotaciones de FlightRequest.
     * - predictionService.getPredictionModel(request) → llama al servicio que integra con el modelo
     *   de Data Science (ONNX) y obtiene la predicción.
     * - ResponseEntity.ok(response) → devuelve la respuesta HTTP 200 con el objeto PredictResponse,
     *   que contiene el resultado de la predicción (ej. "Puntual" o "Retrasado" y su probabilidad).
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictResponse> predict(@Valid @RequestBody  FlightRequest request) throws IOException {
        // Llamamos al servicio que integra con el modelo de Data Science
        PredictResponse response = predictionService.getPredictionModel(request);
        return ResponseEntity.ok(response);
    }
}
