package com.flight_booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {
    private String name;
    private String gender;
    private int age;
    private String seatPreference;  // optional

    @Column(name = "passport_number", unique = true) 
    private String passportNumber;   // unique for passenger
}
