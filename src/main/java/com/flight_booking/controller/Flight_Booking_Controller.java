package com.flight_booking.controller;

import com.flight_booking.entity.Booking;
import com.flight_booking.entity.Flight_Info;
import com.flight_booking.model.BookRequest;
import com.flight_booking.model.CreateFlight;
import com.flight_booking.model.FlightPageResponse;
import com.flight_booking.model.Flight_Booking_Response;
import com.flight_booking.model.SearchRequest;
import com.flight_booking.service.Flight_Booking_Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class Flight_Booking_Controller {

    @Autowired
    private Flight_Booking_Service service;
 
    public Flight_Booking_Controller(Flight_Booking_Service service) {
        this.service = service;
    }
    /* *
     * Create a flight:
     * POST /api/flights
     * body: BookRequest JSON
     * */
    @PostMapping("/flights")
    public ResponseEntity<Flight_Info> createFlight(@RequestBody CreateFlight req) {
        Flight_Info flight = service.createFlight(req);
        return ResponseEntity.ok(flight);
    }
     
    /* *
     * Search flights By Filters:
     * GET /api/flights
     * query params: Important : [ sourceAirport, destinationAirport, departureDate, passengers ], optional filters : [ minPrice, maxPrice, airline, sortBy, sortDir, page, size ] 
     * */
    @GetMapping("/flights")
    public ResponseEntity<FlightPageResponse> searchFlights(
            @RequestParam String sourceAirport,
            @RequestParam String destinationAirport,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) LocalTime departureTime,
            @RequestParam(required = false) LocalTime arrivalTime,    
            @RequestParam(required = false, defaultValue = "1") Integer passengers,
            @RequestParam(required = false) Optional<Double> minPrice,
            @RequestParam(required = false) Optional<Double> maxPrice,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false, defaultValue = "100") Integer durationMinutes,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        // Safely unwrap Optionals
        Double min = minPrice.orElse(null);
        Double max = maxPrice.orElse(null);

        SearchRequest req = new SearchRequest(
                sourceAirport,
                destinationAirport,
                departureDate,
                passengers,
                min,
                max,
                departureTime,
                arrivalTime,
                durationMinutes,
                airline,
                sortBy,
                sortDir
        );

        // Call service to get paginated flight responses
        Page<Flight_Booking_Response> result = service.searchFlights(req, page, size);

        // Wrap in stable JSON response
        FlightPageResponse response = new FlightPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );

        return ResponseEntity.ok(response);
  }
 
    /* *
     * Get Flight Information By flight No
     * GET /api//flight/{flightNo}
     * */
    @GetMapping("/flight/{flightNo}")
    public ResponseEntity<Flight_Info> getFlight(@PathVariable String flightNo) {
    	
    	return service.getFlightByNumber(flightNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Book a flight:
     * POST /api/bookings
     * body: BookRequest JSON
     */
    @PostMapping("/bookings")
    public ResponseEntity<?> bookFlight(@RequestBody BookRequest req) {
        try {
            Booking booking = service.bookFlight(req);
            return ResponseEntity.ok(booking);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Check @ Duplicate passenger detected");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Something went wrong");
            return ResponseEntity.badRequest().body(error);
        }
    }



    /* *
     * Get Booking Information By Booking No
     * GET /api//flight/{flightNo}
     * */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        return service.getBooking(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

   
}
