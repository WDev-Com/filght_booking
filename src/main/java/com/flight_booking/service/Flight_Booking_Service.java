package com.flight_booking.service;

import com.flight_booking.entity.Booking;
import com.flight_booking.entity.Flight_Info;
import com.flight_booking.entity.Passenger;
import com.flight_booking.model.BookRequest;
import com.flight_booking.model.CreateFlight;
import com.flight_booking.model.Flight_Booking_Response;
import com.flight_booking.model.SearchRequest;
import com.flight_booking.repo.Booking_Repo;
import com.flight_booking.repo.Flight_Booking_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class Flight_Booking_Service {

    @Autowired
    private Flight_Booking_Repo flightRepo;

    @Autowired
    private Booking_Repo bookingRepo;
    

    public Flight_Booking_Service(Booking_Repo bookingRepo, Flight_Booking_Repo flightRepo) {
        this.bookingRepo = bookingRepo;
        this.flightRepo = flightRepo;
    }
    
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

/*---------------------SEARCH FLIGHTS-------------------*/
    public Page<Flight_Booking_Response> searchFlights(SearchRequest req, int page, int size) {

        // ---- 1) Build pageable with sort ----
        Sort sort = Sort.unsorted();
        if (req.getSortBy() != null && !req.getSortBy().isBlank()) {
            String sortProp = switch (req.getSortBy()) {
                case "price"     -> "price";
                case "duration"  -> "durationMinutes";
                case "departure" -> "departureDate";   // sort by departure date
                case "arrival"   -> "arrivalDate";
                default          -> "price";
            };
            sort = Sort.by(Sort.Direction.fromString(req.getSortDir()), sortProp);
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        // ---- 2) Basic DB query: only mandatory filters ----
        Page<Flight_Info> pageData = flightRepo
                .findBySourceAirportAndDestinationAirport(
                        req.getSourceAirport(),
                        req.getDestinationAirport(),
                        pageable);

        // ---- 3) In-memory filters for optional params ----
        List<Flight_Info> filtered = pageData.getContent().stream()

                // price range
                .filter(f -> req.getMinPrice() == null || f.getPrice() >= req.getMinPrice())
                .filter(f -> req.getMaxPrice() == null || f.getPrice() <= req.getMaxPrice())

                // airline
                .filter(f -> req.getAirline() == null || req.getAirline().isBlank()
                        || f.getAirline().equalsIgnoreCase(req.getAirline()))

                // departure date
                .filter(f -> req.getDepartureDate() == null || req.getDepartureDate().isBlank()
                        || f.getDepartureDate().toString().equals(req.getDepartureDate()))

                // departure time (LocalTime)
                .filter(f -> req.getDepartureTime() == null
                        || !f.getDepartureTime().isBefore(req.getDepartureTime()))

                // arrival time (LocalTime)
                .filter(f -> req.getArrivalTime() == null
                        || !f.getArrivalTime().isAfter(req.getArrivalTime()))

                .collect(Collectors.toList());

        // ---- 4) Map entities to response DTO ----
        List<Flight_Booking_Response> responses = filtered.stream()
                .map(f -> Flight_Booking_Response.builder()
                        .id(f.getId())
                        .airline(f.getAirline())
                        .flightNumber(f.getFlightNumber())
                        .sourceAirport(f.getSourceAirport())
                        .destinationAirport(f.getDestinationAirport())
                        .departureDate(f.getDepartureDate().format(dateFormatter))
                        .departureTime(f.getDepartureTime().format(timeFormatter))
                        .arrivalDate(f.getArrivalDate().format(dateFormatter))
                        .arrivalTime(f.getArrivalTime().format(timeFormatter))
                        .durationMinutes(f.getDurationMinutes())
                        .price(f.getPrice())
                        .availableSeats(f.getAvailableSeats())
                        .build())
                .collect(Collectors.toList());

        // ---- 5) Return Page with filtered results ----
        // totalElements is filtered.size() to reflect actual number after filtering
        return new PageImpl<>(responses, pageable, filtered.size());
    }

    
    
/*-----------------GET FLIGHTS BY ID----------------------*/
    public Optional<Flight_Info> getFlightByNumber(String flightNumber) {
        return flightRepo.findByFlightNumberIgnoreCase(flightNumber);
    }
    
/*-----------------CREATE NEW FLIGHT----------------------*/   
    @Transactional
    public Flight_Info createFlight(CreateFlight req) {
        Flight_Info flight = Flight_Info.builder()
                .airline(req.getAirline())
                .flightNumber(req.getFlightNumber())
                .sourceAirport(req.getSourceAirport())
                .destinationAirport(req.getDestinationAirport())
                .departureDate(req.getDepartureDate())
                .departureTime(req.getDepartureTime())
                .arrivalDate(req.getArrivalDate())
                .arrivalTime(req.getArrivalTime())
                .durationMinutes(req.getDurationMinutes())
                .price(req.getPrice())
                .availableSeats(req.getAvailableSeats())
                .build();

        return flightRepo.save(flight);
    }

 /*------------------BOOK FLIGHT---------------------*/
    @Transactional
    public Booking bookFlight(BookRequest req) {
    try {
        // 1) Defensive null checks
        if (req == null) throw new RuntimeException("Check 1 : Request cannot be null");
        if (req.getPassengers() == null || req.getPassengers().isEmpty())
            throw new RuntimeException("Check 2 : At least one passenger is required");

        // 2) Flight existence & seat availability
        String flightNo = req.getFlightNumber();
        if (flightNo == null || flightNo.trim().isEmpty()) {
            throw new RuntimeException("Check 3 : Flight number is required");
        }

        Flight_Info flight = flightRepo.findByFlightNumberIgnoreCase(flightNo)
                .orElseThrow(() -> new RuntimeException("Check 4 : Flight not found: " + flightNo));

        int passengerCount = req.getPassengers().size();
        if (flight.getAvailableSeats() < passengerCount) {
            throw new RuntimeException("Check 5 : Not enough seats available");
        }

        // 3) Check if user already booked the same flight
        if (bookingRepo.existsByUserIdAndFlightNumber(req.getUserId(), flightNo)) {
            throw new RuntimeException("Check 6 : User has already booked this flight");
        }

        // 4) Check duplicate passports inside request
        List<String> passports = req.getPassengers()
                .stream()
                .map(Passenger::getPassportNumber)
                .toList();

        long distinctCount = passports.stream().distinct().count();
        if (distinctCount != passports.size()) {
            throw new RuntimeException("Check 7.1 : Duplicate passport numbers in request are not allowed");
        }

        List<Booking> conflicts = bookingRepo.findExistingBookingsByPassport(passports, flightNo);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException(
                    "Check 7.2 : One or more passengers (by passport number) are already booked on another flight");
        }

        // 5) Deduct seats
        flight.setAvailableSeats(flight.getAvailableSeats() - passengerCount);
        flightRepo.save(flight);

        // 6) Save booking
        Booking booking = Booking.builder()
                .userId(req.getUserId())
                .flightNumber(flight.getFlightNumber())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .passengers(req.getPassengers())
                .passengerCount(passengerCount)
                .totalAmount(passengerCount * flight.getPrice())
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        return bookingRepo.save(booking);

    } catch (DataIntegrityViolationException e) {
        // Clean message for duplicate passport number
        throw new RuntimeException("Check $.1 : One or more passengers already exist in another booking (duplicate passport detected).");
    } catch (TransactionSystemException e) {
        throw new RuntimeException("Check $.2 : Transaction failed due to invalid data.");
    } catch (Exception e) {
        throw new RuntimeException("Check $.3 : Unexpected error - " + e.getMessage());
    }
}


   
/*-------------------GET BOOKING DETAILS------------------------*/
    public Optional<Booking> getBooking(Long id) {
        return bookingRepo.findById(id);
    }
}
