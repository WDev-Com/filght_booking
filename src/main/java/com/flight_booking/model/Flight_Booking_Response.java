package com.flight_booking.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Flight_Booking_Response {

    private Long id;
    private String airline;
    private String flightNumber;
    private String sourceAirport;
    private String destinationAirport;
    
    private String departureDate;
    private String departureTime;
    private String arrivalDate;
    private String arrivalTime;

    private int durationMinutes;
    private double price;
    private int availableSeats;
}
