package com.flight_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use flightNumber as reference
    private String flightNumber;
    @Column(nullable = false)
    private Long userId;
    private String contactEmail;
    private String contactPhone;

    private LocalDateTime bookingTime;
    private int passengerCount;
    private double totalAmount;
    private String status; // e.g., "PENDING", "CONFIRMED"

    @ElementCollection
    @CollectionTable(name = "booking_passengers", joinColumns = @JoinColumn(name = "booking_id"))
    private List<Passenger> passengers;
}
