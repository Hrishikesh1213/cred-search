package com.cred.search.core.dao;


import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.Seat;
import com.cred.search.models.commons.enums.Tier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InventoryDao {

    Set<Route> findAllPhysicalRoutes();

    Set<City> findAllCities();

    Route addPhysicalRoute(String source, String destination, String flight,
                           Long departureTimeEpoch, Long arrivalTimeEpoch, Long durationMinutes,
                           Map<Tier, Integer> unbookedSeatCount,
                           Map<Tier, Double> price, Map<Tier, List<Seat>> seatingMap
    );
}
