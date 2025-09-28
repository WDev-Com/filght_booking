package com.flight_booking.repo;

import com.flight_booking.entity.Flight_Info;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface Flight_Booking_Repo extends JpaRepository<Flight_Info, Long> {
    // simple search by source/destination - date handling would be by departureTime string or separate column for date.
    Page<Flight_Info> findBySourceAirportAndDestinationAirport(
            String source, String destination, Pageable pageable);

    List<Flight_Info> findBySourceAirportAndDestinationAirport(String source, String destination);
    
    // search by flightNumber
    @Query("select f from Flight_Info f where upper(f.flightNumber) = upper(:flightNumber)")
    Optional<Flight_Info> findByFlightNumberIgnoreCase(@Param("flightNumber") String flightNumber);}
