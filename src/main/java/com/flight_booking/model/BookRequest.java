package com.flight_booking.model;

import com.flight_booking.entity.Passenger;
import lombok.*;

import java.util.List;

@Builder
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
	private Long userId; 
    private String flightNumber;       // Flight_Info.flightNumber
    private List<Passenger> passengers;
    private String contactEmail;
    private String contactPhone;
}
