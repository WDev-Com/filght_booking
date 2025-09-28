// FlightPageResponse.java
package com.flight_booking.model;

import java.util.List;

public record FlightPageResponse(
        List<Flight_Booking_Response> flights,
        int page,
        int size,
        long totalElements
) {}
