package com.cred.search.core.manager.impl;

import com.cred.search.core.dao.InventoryDao;
import com.cred.search.core.exception.SearchServiceException;
import com.cred.search.core.manager.IInventoryManager;
import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.Seat;
import com.cred.search.models.commons.enums.Tier;
import com.cred.search.models.request.PhysicalRouteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class InventoryManagerImpl implements IInventoryManager {

    private final InventoryDao inventoryDao;

    @Override
    public Set<Route> findAllPhysicalRoutes() {
        return inventoryDao.findAllPhysicalRoutes();
    }

    @Override
    public Set<City> getAllCities() {
        return inventoryDao.findAllCities();
    }

    @Override
    public Route addPhysicalRoute(PhysicalRouteRequest route) {

        List<Seat> seats = route.getSeats();

        Map<Tier, List<Seat>> seatingMap = seats.stream()
                .collect(Collectors.groupingBy(Seat::getTier));

        Map<Tier, Integer> unbookedSeatCountMap = new HashMap<>();
        Map<Tier, Double> priceMap = new HashMap<>();

        seatingMap.forEach((tier, tierSeats) -> {
            long unbookedCount = tierSeats.stream().filter(seat -> !Boolean.TRUE.equals(seat.getIsBooked())).count();
            unbookedSeatCountMap.put(tier, (int) unbookedCount);
            double minPrice = tierSeats.stream()
                    .mapToDouble(Seat::getPrice)
                    .min()
                    .orElseThrow(() -> new SearchServiceException("No price for seat found for tier: " + tier));
            priceMap.put(tier, minPrice);
        });

        return inventoryDao.addPhysicalRoute(
                route.getSourceCityId(),
                route.getDestinationCityId(),
                route.getFlightId(),
                route.getDepartureTimeEpoch(),
                route.getArrivalTimeEpoch(),
                route.getDurationMinutes(),
                unbookedSeatCountMap,
                priceMap,
                seatingMap
        );
    }
}
