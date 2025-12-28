package com.cred.search.models.request;

import com.cred.search.models.commons.Seat;
import lombok.Data;

import java.util.List;

@Data
public class PhysicalRouteRequest {
    private String sourceCityId;

    private String destinationCityId;

    private String flightId;

    private Long departureTimeEpoch;

    private Long arrivalTimeEpoch;

    private Long durationMinutes;

    private List<Seat> seats;

}
