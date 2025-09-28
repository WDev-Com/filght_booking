package com.flight_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Flight_Info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String airline;            
    private String flightNumber;       
    private String sourceAirport;      
    private String destinationAirport; 
   
    private LocalDate departureDate;
    @Column(columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime departureTime;

    private LocalDate arrivalDate;
    @Column(columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime arrivalTime;

    private int durationMinutes;
    private double price;             
    private int availableSeats;
}
