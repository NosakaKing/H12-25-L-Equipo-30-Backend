package com.h12_25.flightontime.services;

import ai.onnxruntime.*;
import com.h12_25.flightontime.client.DsClient;
import com.h12_25.flightontime.dto.FlightRequest;
import com.h12_25.flightontime.dto.PredictResponse;
import com.h12_25.flightontime.entity.Flightontime;
import com.h12_25.flightontime.entity.PredictionResponse;
import com.h12_25.flightontime.repository.IFlightontimeRepository;
import com.h12_25.flightontime.repository.IPredictionResponseRepository;
import com.h12_25.flightontime.util.RiskMapLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.h12_25.flightontime.util.RiskMapLoader.*;

@Service
public class PredictionService {

    private DsClient dsClient;
    private OrtEnvironment env;
    private OrtSession session;
    private RiskMapLoader riskMapLoader;

    @Autowired
    private  IFlightontimeRepository flightontimeRepository;

    @Autowired
    private IPredictionResponseRepository predictionRepository;

    public PredictionService() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession("src/main/resources/flight_delay_rf_weighted.onnx");
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
            return new PredictResponse("Error", 0.0F);
        }
    }

    public PredictResponse getPredictionModel(FlightRequest dto) throws IOException {
        // Carga los mapas de riesgo desde archivos JSON externos:
        // - carrier_map.json → riesgos por aerolínea
        // - airport_map.json → riesgos por aeropuerto
        // - dep_time_map.json → riesgos por bloque horario
        Map<String, Float> carrierMap = RiskMapLoader.loadMap("carrier_map.json");
        Map<String, Float> airportMap = RiskMapLoader.loadMap("airport_map.json");
        Map<String, Float> depTimeMap = RiskMapLoader.loadMap("dep_time_map.json");

        // 1. Convertir el código de aerolínea (ej. "AA") al nombre completo
        String carrierKey = carrierCodeToName.get(dto.CARRIER_NAME());
        // 2. Convertir el código de aeropuerto (ej. "MIA") al nombre completo
        String airportKey = airportCodeToName.get(dto.DEPARTING_AIRPORT());
        // 3. Convertir el bloque de hora (ej. "1800-1859") a la clave usada en el mapa
        String depTimeKey = depTimeCodeToBlock.get(dto.DEP_TIME_BLK());
        // 4. Buscar el valor de riesgo de esa aerolínea en carrier_map.json

        Float carrierValue = carrierMap.get(carrierKey);
        // 5. Buscar el valor de riesgo de ese aeropuerto en airport_map.json
        Float airportValue = airportMap.get(airportKey);
        // 6. Buscar el valor de riesgo de ese bloque horario en dep_time_map.json
        Float depTimeValue = depTimeMap.get(depTimeKey);

        if (carrierValue == null) {
            throw new IllegalArgumentException("No se encontró en el carrier: " + dto.CARRIER_NAME());
        }
        if (airportValue == null) {
            throw new IllegalArgumentException("No se encontró el aeropuerto: " + dto.DEPARTING_AIRPORT());
        }
        if (depTimeValue == null) {
            throw new IllegalArgumentException("No se encontró departamento: " + dto.DEP_TIME_BLK());
        }

        // Construcción del arreglo de entrada (inputData) para el modelo ONNX.
// Cada fila representa un vuelo con sus características numéricas:
// - MONTH: mes del vuelo
// - DAY_OF_WEEK: día de la semana
// - DISTANCE_GROUP: grupo de distancia del vuelo
// - SEGMENT_NUMBER: número de segmento
// - CONCURRENT_FLIGHTS: cantidad de vuelos concurrentes
// - PRCP: precipitación
// - TMAX: temperatura máxima
// - AWND: velocidad del viento
// - PLANE_AGE: edad del avión
// - AIRPORT_FLIGHTS_MONTH: cantidad de vuelos en el aeropuerto ese mes
// - carrierValue: riesgo asociado a la aerolínea (desde carrier_map.json)
// - airportValue: riesgo asociado al aeropuerto (desde airport_map.json)
// - depTimeValue: riesgo asociado al bloque horario (desde dep_time_map.json)
//
// Este arreglo se convierte en un tensor y se pasa como input al modelo.
        try {
            float[][] inputData = new float[][] {
                    {
                            dto.MONTH().floatValue(),
                            dto.DAY_OF_WEEK().floatValue(),
                            dto.DISTANCE_GROUP().floatValue(),
                            dto.SEGMENT_NUMBER().floatValue(),
                            dto.CONCURRENT_FLIGHTS().floatValue(),
                            dto.PRCP().floatValue(),
                            dto.TMAX().floatValue(),
                            dto.AWND().floatValue(),
                            dto.PLANE_AGE().floatValue(),
                            dto.AIRPORT_FLIGHTS_MONTH().floatValue(),
                            carrierValue,
                            airportValue,
                            depTimeValue
                    }
            };

            //  Crear un tensor ONNX a partir del arreglo de entrada (inputData).
            //  Este tensor es el formato que ONNX Runtime necesita para procesar los datos.
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
            //   Construir un mapa de entradas para el modelo.
           //    La clave "float_input" debe coincidir con el nombre de la entrada definido en el modelo ONNX.
           //    El valor es el tensor que acabamos de crear.
            Map<String, OnnxTensor> inputs = Map.of("float_input", inputTensor);

            //   Ejecutar la sesión del modelo con las entradas.
           //    Esto corre la inferencia y devuelve un objeto Result con las salidas del modelo.
            OrtSession.Result result = session.run(inputs);

            // salida 0: clase predicha
            long[] label = (long[]) result.get(0).getValue();
            int predictedClass = (int) label[0];

            // salida 1: secuencia con un OnnxMap
            OnnxSequence seq = (OnnxSequence) result.get(1);
            List<?> values = seq.getValue();

            OnnxMap probsMap = (OnnxMap) values.get(0);
            Map<?, ?> rawMap = probsMap.getValue();

            Map<String, Float> probs = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                probs.put(entry.getKey().toString(), ((Number) entry.getValue()).floatValue());
            }

            // Obtener la probabilidad y la predicción textual del modelo:
           // - probs.get(String.valueOf(predictedClass)) → busca en el mapa la probabilidad asociada
           //   a la clase predicha (0 = Puntual, 1 = Retrasado).
           // - prevision → convierte la clase numérica en un texto entendible ("Puntual" o "Retrasado").
            Float probabilidad = probs.get(String.valueOf(predictedClass));
            String prevision = predictedClass == 1 ? "Retrasado" : "Puntual";


            // Guardar en la base de datos
            Flightontime entity = new Flightontime();
            entity.setMONTH(dto.MONTH());
            entity.setDAY_OF_WEEK(dto.DAY_OF_WEEK());
            entity.setDISTANCE_GROUP(dto.DISTANCE_GROUP());
            entity.setSEGMENT_NUMBER(dto.SEGMENT_NUMBER());
            entity.setCONCURRENT_FLIGHTS(dto.CONCURRENT_FLIGHTS());
            entity.setPRCP(dto.PRCP());
            entity.setTMAX(dto.TMAX());
            entity.setAWND(dto.AWND());
            entity.setPLANE_AGE(dto.PLANE_AGE());
            entity.setAIRPORT_FLIGHTS_MONTH(dto.AIRPORT_FLIGHTS_MONTH());
            entity.setCARRIER_NAME(dto.CARRIER_NAME());
            entity.setDEPARTING_AIRPORT(dto.DEPARTING_AIRPORT());
            entity.setDEP_TIME_BLK(dto.DEP_TIME_BLK());

            flightontimeRepository.save(entity);

            //guardar predicion en la base
            PredictionResponse predictionResponse = new PredictionResponse();
            predictionResponse.setPrevision(prevision);
            predictionResponse.setProbabilidad(probabilidad);
            predictionResponse.setFlight(entity);

            predictionRepository.save(predictionResponse);


            return new PredictResponse(prevision, probabilidad);

        } catch (Exception e) {
            return new PredictResponse("Error", 0.0F);
        }
    }

}
