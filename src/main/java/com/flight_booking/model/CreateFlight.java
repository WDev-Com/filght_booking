package com.flight_booking.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlight {
    private String airline;
    private String flightNumber;
    private String sourceAirport;
    private String destinationAirport;

    private LocalDate departureDate;
    private LocalTime departureTime;

    private LocalDate arrivalDate;
    private LocalTime arrivalTime;

    private int durationMinutes;
    private double price;
    private int availableSeats;
}
