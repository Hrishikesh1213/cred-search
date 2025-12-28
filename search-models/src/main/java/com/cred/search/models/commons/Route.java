package com.cred.search.models.commons;

import com.cred.search.models.commons.enums.Tier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    // --- Identifiers ---
    private String routeId; // LogicalRoute ID (for parent) or PhysicalRoute ID (for subPaths)

    // --- Core Flight/Path Details ---
    private City source;      // e.g. "Mumbai"

    private City destination;     // e.g. "New Delhi"

    private Flight flight;    // e.g. "AI-102" (Null for parent, populated for connecting flights)

    // --- Time Data (Using Epoch for consistency with Entities) ---
    private Long departureTimeEpoch;
    private Long arrivalTimeEpoch;
    private Long durationMinutes;

    // --- Logic Flags ---
    private Boolean isDirectFlight;
    private Integer numberOfStops; // 0, 1, or 2

    // --- Hierarchy ---
    // If isDirectFlight = false, this list contains the segments (Physical Routes).
    // If isDirectFlight = true, this can be null or empty.
    // (populated for parent, null for connecting flights)
    private List<Route> subPaths;

    // --- Inventory & Pricing ---

    // KEY: For the Parent Route:
    // 1. unbookedSeatCount = MIN(unbookedSeatCount of all subPaths)
    // 2. price = SUM(price of all subPaths)
    // 3. isFullyBooked = OR(isFullyBooked of all subPaths) [If any leg is full, the whole path is full]

    private Map<Tier, Integer> unbookedSeatCount;
    private Map<Tier, Boolean> isFullyBooked;

    // Aggregated price (e.g., sum of cheapest seats in this tier)
    private Map<Tier, Double> price;

    // Detailed seat map.
    // Optimization Note: Usually only populated for `subPaths` or when user clicks "View Seats".
    // Populating this for every search result in the list view is performance heavy.
    // (Null for parent, populated for connecting flights)
    private Map<Tier, List<Seat>> seatingMap;

}