package com.h12_25.flightontime.dto;

import java.time.LocalDateTime;

public record FlightRequest(
        String aerolinea,
        String origen,
        String destino,
        String fecha_partida,
        Double distancia_km
) {
}
