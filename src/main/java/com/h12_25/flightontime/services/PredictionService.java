package com.h12_25.flightontime.services;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.h12_25.flightontime.client.DsClient;
import com.h12_25.flightontime.dto.FlightRequest;
import com.h12_25.flightontime.dto.PredictResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PredictionService {

    private DsClient dsClient;
    private OrtEnvironment env;
    private OrtSession session;

    public PredictionService() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession("src/main/resources/modelo.onnx");
    }

    public PredictResponse getPredictionFastAPI(FlightRequest request) {
        try {
            // Llamada al microservicio Python
            PredictResponse response = dsClient.predict(request);

            // Validación adicional: probabilidad entre 0 y 1
            if (response.probabilidad() < 0 || response.probabilidad() > 1) {
                throw new IllegalStateException("Probabilidad fuera de rango");
            }
            return response;
        } catch (Exception e) {
            // Manejo de errores: puedes loguear y devolver un valor por defecto
            return new PredictResponse("Error", 0.0);
        }
    }

    public PredictResponse getPredictionModel(FlightRequest request) {
        try {
            // Convertir datos de entrada a tensor
            float[][] inputData = {
                    {
                            // ejemplo: hora_min, dia_semana, mes, distancia_km
                            (float) 870, // 14:30 → 870 minutos
                            (float) 1,   // lunes
                            (float) 11,  // noviembre
                            request.distancia_km().floatValue()
                    }
            };

            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
            Map<String, OnnxTensor> inputs = Map.of("input", inputTensor);

            OrtSession.Result result = session.run(inputs);
            float[][] output = (float[][]) result.get(0).getValue();

            double probabilidad = output[0][1]; // clase "Retrasado"
            String prevision = probabilidad >= 0.5 ? "Retrasado" : "Puntual";

            return new PredictResponse(prevision, probabilidad);

        } catch (Exception e) {
            return new PredictResponse("Error", 0.0);
        }
    }

}
