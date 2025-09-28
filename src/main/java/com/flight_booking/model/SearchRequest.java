package com.flight_booking.model;

import java.time.LocalTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    private String sourceAirport;      // matches Flight_Info.sourceAirport
    private String destinationAirport; // matches Flight_Info.destinationAirport
    private String departureDate;      // "2025-09-26", simple date handling
    private Integer passengers;        // number of passengers

    // optional filters
    private Double minPrice;
    private Double maxPrice;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private int durationMinutes;
    private String airline;    // matches Flight_Info.airline
    private String sortBy;     // price | duration | departure | arrival
    private String sortDir;    // asc | desc
}
