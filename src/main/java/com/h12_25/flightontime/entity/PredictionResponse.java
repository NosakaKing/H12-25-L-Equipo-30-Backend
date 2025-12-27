package com.h12_25.flightontime.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "PredictionResponse")
@Table(name = "prediction_response")
@Data
public class PredictionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prevision;
    private Float probabilidad;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flightontime flight;

}
