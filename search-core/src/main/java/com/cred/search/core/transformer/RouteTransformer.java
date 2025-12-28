package com.cred.search.core.transformer;

import com.cred.search.core.dao.entity.*;
import com.cred.search.core.dao.repository.CityRepository;
import com.cred.search.core.dao.repository.FlightRepository;
import com.cred.search.core.dao.repository.SeatRepository;
import com.cred.search.core.exception.SearchServiceException;
import com.cred.search.core.util.HashUtil;
import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Flight;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.Seat;
import com.cred.search.models.commons.enums.Tier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class RouteTransformer {

    private final CityRepository cityRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    public Route toRouteModel(LogicalRouteEntity entity) {
        if (entity == null) return null;

        // 1. Map JSONB List<Map> -> List<Route> (Sub-paths)
        List<Map<String, Object>> legsData = entity.getRouteLegs() != null
                ? entity.getRouteLegs()
                : Collections.emptyList();

        List<Route> subPaths = legsData.stream()
                .map(this::toLegRouteModel)
                .toList();

        Map<Tier, Integer> seatCount = new HashMap<>();
        if (entity.getAvailableSeatsMin() != null) {
            for (Map.Entry<String, Object> entry : entity.getAvailableSeatsMin().entrySet()) {
                try {
                    Tier tier = Tier.valueOf(entry.getKey());
                    Integer count = ((Number) entry.getValue()).intValue();
                    seatCount.put(tier, count);
                } catch (Exception e) { /* ignore */ }
            }
        }

        // 2. NEW: Convert Price Map (JSON -> Map<Tier, Double>)
        Map<Tier, Double> priceMap = new HashMap<>();
        if (entity.getPrices() != null) {
            for (Map.Entry<String, Object> entry : entity.getPrices().entrySet()) {
                try {
                    Tier tier = Tier.valueOf(entry.getKey());
                    // Safe cast for JSON numbers (Integer vs Double)
                    Double price = ((Number) entry.getValue()).doubleValue();
                    priceMap.put(tier, price);
                } catch (Exception e) { /* ignore */ }
            }
        }

        return Route.builder()
                .routeId(entity.getId())
                .source(toCityModel(entity.getSource()))
                .destination(toCityModel(entity.getDestination()))
                .departureTimeEpoch(entity.getStartTimeInEpoch())
                .arrivalTimeEpoch(entity.getStartTimeInEpoch() + entity.getTotalDurationInMillis())
                .durationMinutes(entity.getTotalDurationInMillis() / 60000)
                .isDirectFlight(entity.getNumberOfStops() == 0)
                .numberOfStops(entity.getNumberOfStops())
                .subPaths(subPaths) // Defined in your existing code
                .unbookedSeatCount(seatCount)
                .price(priceMap) // <--- Set the mapped prices here
                .build();
    }

    /**
     * Helper: Converts a single JSON Map (representing a Leg) into a Route object
     */
    private Route toLegRouteModel(Map<String, Object> legMap) {
        if (legMap == null) return null;

        // Safely extract fields from the Map
        String flightNumber = (String) legMap.get("flightNumber");
        String operator = (String) legMap.get("operator");
        String sourceCode = (String) legMap.get("source");
        String destCode = (String) legMap.get("destination");

        // Use Number casting to be safe (JSON integers might come as Integer or Long)
        long departureEpoch = ((Number) legMap.get("departureTimeEpoch")).longValue();
        long arrivalEpoch = ((Number) legMap.get("arrivalTimeEpoch")).longValue();
        long durationMins = ((Number) legMap.get("durationMinutes")).longValue();

        Flight flightInfo = Flight.builder()
                .id(flightNumber)
                .operator(operator)
                .build();

        // Construct simple City objects (since we only have codes in the JSON)
        City sourceCity = City.builder().id(sourceCode).name(sourceCode).build();
        City destCity = City.builder().id(destCode).name(destCode).build();

        return Route.builder()
                .routeId(flightNumber)
                .source(sourceCity)
                .destination(destCity)
                .flight(flightInfo)
                .departureTimeEpoch(departureEpoch)
                .arrivalTimeEpoch(arrivalEpoch)
                .durationMinutes(durationMins)
                .isDirectFlight(true)
                .subPaths(null)
                .seatingMap(Collections.emptyMap())
                .build();
    }

    public City toCityModel(CityEntity entity) {
        if (entity == null) return null;
        return City.builder()
                .id(entity.getCode())
                .name(entity.getName())
                .build();
    }

    /**
     * Converts a Route Model back to an Entity (Used for creating routes)
     */
    public LogicalRouteEntity toLogicalEntity(Route route) {
        CityEntity source = cityRepository.findById(route.getSource().getId())
                .orElseThrow(() -> new RuntimeException("Source City not found: " + route.getSource().getId()));

        CityEntity dest = cityRepository.findById(route.getDestination().getId())
                .orElseThrow(() -> new RuntimeException("Dest City not found: " + route.getDestination().getId()));

        // 1. Convert sub-paths back to List<Map<String, Object>>
        List<Map<String, Object>> legsList = new ArrayList<>();
        if (route.getSubPaths() != null) {
            for (Route sub : route.getSubPaths()) {
                Map<String, Object> legMap = new HashMap<>();
                legMap.put("flightNumber", sub.getFlight().getId());
                legMap.put("operator", sub.getFlight().getOperator());
                legMap.put("source", sub.getSource().getId());
                legMap.put("destination", sub.getDestination().getId());
                legMap.put("departureTimeEpoch", sub.getDepartureTimeEpoch());
                legMap.put("arrivalTimeEpoch", sub.getArrivalTimeEpoch());
                legMap.put("durationMinutes", sub.getDurationMinutes());
                legsList.add(legMap);
            }
        }

        // 2. Convert Seat Map back to Map<String, Object>
        Map<String, Object> seatMap = new HashMap<>();
        if (route.getUnbookedSeatCount() != null) {
            route.getUnbookedSeatCount().forEach((tier, count) ->
                    seatMap.put(tier.name(), count)
            );
        }

        // 2. Convert Price Map back to Map<String, Object>
        Map<String, Object> priceMap = new HashMap<>();
        if (route.getPrice() != null) {
            route.getPrice().forEach((tier, price) ->
                    priceMap.put(tier.name(), price)
            );
        }

        return LogicalRouteEntity.builder()
                .id(HashUtil.generateHash(legsList))
                .source(source)
                .destination(dest)
                .startTimeInEpoch(route.getDepartureTimeEpoch())
                .totalDurationInMillis(route.getDurationMinutes() * 60000)
                .routeLegs(legsList)        // Store as JSON
                .availableSeatsMin(seatMap) // Store as JSON
                .numberOfStops(Math.max(0, legsList.size() - 1))
                .prices(priceMap)
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();
    }

    // --- Utility: Merge Logic (Preserved from your snippet) ---
    public Route mergeRoute(ArrayList<Route> suitableRoute, City source, City destination) throws JsonProcessingException {
        if (suitableRoute == null || suitableRoute.isEmpty()) return null;

        Long startTime = suitableRoute.get(0).getDepartureTimeEpoch();
        Long endTime = suitableRoute.get(suitableRoute.size() - 1).getArrivalTimeEpoch();
        Long durationMinutes = (endTime - startTime) / 1000 / 60;

        // Calculate Min Seats available across all legs
        Map<Tier, Integer> unbookedSeatCount = new HashMap<>();
        for (Route leg : suitableRoute) {
            for (Tier tier : Tier.values()) {
                int currentLegSeats = leg.getUnbookedSeatCount().getOrDefault(tier, Integer.MAX_VALUE);
                int minSoFar = unbookedSeatCount.getOrDefault(tier, Integer.MAX_VALUE);

                // Initialize if empty, otherwise take min
                if (!unbookedSeatCount.containsKey(tier)) {
                    unbookedSeatCount.put(tier, currentLegSeats);
                } else {
                    unbookedSeatCount.put(tier, Math.min(minSoFar, currentLegSeats));
                }
            }
        }

        // Correct MAX_VALUE artifacts if a tier wasn't found
        unbookedSeatCount.replaceAll((t, v) -> v == Integer.MAX_VALUE ? 0 : v);

        Map<Tier, Boolean> isFullyBooked = Arrays.stream(Tier.values())
                .collect(Collectors.toMap(t -> t, t -> unbookedSeatCount.getOrDefault(t, 0) == 0));

        // Price = SUM of all sub-path prices
        Map<Tier, Double> price = new HashMap<>();
        for (Route route : suitableRoute) {
            for (Tier tier : Tier.values()) {
                Double routePrice = route.getPrice() != null ? route.getPrice().getOrDefault(tier, 0.0) : 0.0;
                price.merge(tier, routePrice, Double::sum);
            }
        }
        log.info("this is the price" + new ObjectMapper().writeValueAsString(price));

        return Route.builder()
                .source(source)
                .destination(destination)
                .departureTimeEpoch(startTime)
                .arrivalTimeEpoch(endTime)
                .durationMinutes(durationMinutes)
                .isDirectFlight(suitableRoute.size() == 1)
                .numberOfStops(suitableRoute.size() - 1)
                .subPaths(suitableRoute)
                .unbookedSeatCount(unbookedSeatCount)
                .isFullyBooked(isFullyBooked)
                .price(price)
                .build();
    }

    // --- Physical Entity to Model ---
    public Route toPhysicalRouteModel(PhysicalRouteEntity entity) {
        if (entity == null) return null;

        List<SeatEntity> seatEntities = seatRepository.findAllByPhysicalRoute(entity);

        Map<Tier, List<SeatEntity>> seatsByTier = seatEntities.stream()
                .collect(Collectors.groupingBy(SeatEntity::getTier));

        Map<Tier, Boolean> isFullyBookedMap = new HashMap<>();
        Map<Tier, Integer> unbookedSeatCountMap = new HashMap<>();
        Map<Tier, Double> priceMap = new HashMap<>();

        seatsByTier.forEach((tier, seats) -> {

            long unbookedCount = seats.stream().filter(seat -> !seat.isBooked()).count();
            unbookedSeatCountMap.put(tier, (int) unbookedCount);

            isFullyBookedMap.put(tier, unbookedCount == 0);
            double minPrice = seats.stream()
                    .mapToDouble(SeatEntity::getPrice)
                    .min()
                    .orElseThrow(() -> new SearchServiceException("No price for seat found for tier: " + tier));
            priceMap.put(tier, minPrice);
        });

        Flight flightInfo = Flight.builder()
                .id(entity.getFlight().getFlightNumber())
                .operator(entity.getFlight().getOperator())
                .build();

        return Route.builder()
                .routeId(entity.getId())
                .source(toCityModel(entity.getSource()))
                .destination(toCityModel(entity.getDestination()))
                .flight(flightInfo)
                .departureTimeEpoch(entity.getStartTimeInEpoch())
                .arrivalTimeEpoch(entity.getStartTimeInEpoch() + entity.getDurationInMillis())
                .durationMinutes(entity.getDurationInMillis() / 60000)
                .isDirectFlight(true)
                .subPaths(null)
                .isFullyBooked(isFullyBookedMap)
                .unbookedSeatCount(unbookedSeatCountMap)
                .price(priceMap)
                .seatingMap(Collections.emptyMap())
                .build();
    }

    // --- Route Model to Physical Entity (for saving new physical routes) ---
    public PhysicalRouteEntity toPhysicalEntity(Route route) {
        if (route == null) return null;

        CityEntity source = cityRepository.findById(route.getSource().getId())
                .orElseThrow(() -> new RuntimeException("Source City not found: " + route.getSource().getId()));

        CityEntity destination = cityRepository.findById(route.getDestination().getId())
                .orElseThrow(() -> new RuntimeException("Destination City not found: " + route.getDestination().getId()));

        FlightEntity flight = flightRepository.findById(route.getFlight().getId())
                .orElseThrow(() -> new RuntimeException("Flight not found: " + route.getFlight().getId()));

        long durationInMillis = route.getDurationMinutes() != null 
                ? route.getDurationMinutes() * 60000 
                : (route.getArrivalTimeEpoch() - route.getDepartureTimeEpoch());

        return PhysicalRouteEntity.builder()
                .source(source)
                .destination(destination)
                .flight(flight)
                .startTimeInEpoch(route.getDepartureTimeEpoch())
                .durationInMillis(durationInMillis)
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();
    }

    // Inside RouteTransformer.java

    public SeatEntity toSeatEntity(Seat seatModel, Tier tier, PhysicalRouteEntity physicalRoute) {
        return SeatEntity.builder()
                .physicalRoute(physicalRoute)
                .tier(tier)
                .seatNumber(seatModel.getSeatId()) // Map ID to Seat Number
                .isBooked(seatModel.getIsBooked())
                .price(seatModel.getPrice())
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();
    }

    public Seat toSeatModel(SeatEntity entity) {
        if (entity == null) return null;
        return Seat.builder()
                .seatId(entity.getSeatNumber())
                .tier(entity.getTier())
                .isBooked(entity.isBooked())
                .price(entity.getPrice())
                .userId(entity.isBooked() ? "booked_user" : null) // Placeholder logic
                .build();
    }
}