package com.h12_25.flightontime.repository;

import com.h12_25.flightontime.entity.Flightontime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFlightontimeRepository extends JpaRepository<Flightontime, Long> {
}
