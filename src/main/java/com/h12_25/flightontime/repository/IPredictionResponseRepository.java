package com.h12_25.flightontime.repository;

import com.h12_25.flightontime.entity.PredictionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPredictionResponseRepository extends JpaRepository<PredictionResponse, Long> {
}
