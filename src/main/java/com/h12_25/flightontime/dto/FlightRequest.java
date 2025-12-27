package com.h12_25.flightontime.dto;


public record FlightRequest(
               Integer MONTH,
               Integer DAY_OF_WEEK,
               Integer DISTANCE_GROUP,
               Integer SEGMENT_NUMBER,
               Integer CONCURRENT_FLIGHTS,
               Double PRCP,
               Double TMAX,
               Double AWND,
               Integer PLANE_AGE,
               Integer AIRPORT_FLIGHTS_MONTH,
               String CARRIER_NAME,
               String DEPARTING_AIRPORT,
               String DEP_TIME_BLK
) {
}
