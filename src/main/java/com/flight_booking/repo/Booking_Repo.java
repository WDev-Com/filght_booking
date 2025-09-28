package com.flight_booking.repo;

import com.flight_booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Booking_Repo extends JpaRepository<Booking, Long> {

	// Already existing methods …
    boolean existsByUserIdAndFlightNumber(Long userId, String flightNumber);
    

    /**
     * Find all bookings where at least one passenger’s passport number
     * matches any value in the given list, and the booking is NOT
     * for the specified flight number.
     */
    @Query("""
           select distinct b
           from Booking b
                join b.passengers p
           where p.passportNumber in :passports
             and b.flightNumber <> :flightNumber
           """)
    List<Booking> findExistingBookingsByPassport(
            @Param("passports") List<String> passports,
            @Param("flightNumber") String flightNumber
    );

    // Get all bookings for this user except a specific flight
    @Query("SELECT b FROM Booking b JOIN b.passengers p " +
           "WHERE b.userId = :userId AND b.flightNumber <> :flightNumber " +
           "AND p.name IN :names AND p.age IN :ages AND p.gender IN :genders")
    List<Booking> findUserBookingsWithSamePassengers(
            @Param("userId") Long userId,
            @Param("flightNumber") String flightNumber,
            @Param("names") List<String> names,
            @Param("ages") List<Integer> ages,
            @Param("genders") List<String> genders
    );
    
 

    List<Booking> findByUserId(Long userId);
}
