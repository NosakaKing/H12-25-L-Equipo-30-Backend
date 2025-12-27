package com.h12_25.flightontime.dto;

public record PredictResponse(
        String prevision,
        Float probabilidad
) {
}
