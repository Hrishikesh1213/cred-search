package com.cred.search.core.dao.impl;

import com.cred.search.core.dao.InventoryDao;
import com.cred.search.core.dao.entity.PhysicalRouteEntity;
import com.cred.search.core.dao.repository.CityRepository;
import com.cred.search.core.dao.repository.PhysicalRouteRepository;
import com.cred.search.core.transformer.RouteTransformer;
import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Flight;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.Seat;
import com.cred.search.models.commons.enums.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class InventoryDaoImpl implements InventoryDao {

    private final PhysicalRouteRepository physicalRouteRepository;
    private final CityRepository cityRepository;
    private final RouteTransformer routeTransformer;

    @Override
    public Set<Route> findAllPhysicalRoutes() {
        log.info("DAO: Fetching all physical routes");
        return physicalRouteRepository.findAll()
                .stream()
                .map(routeTransformer::toPhysicalRouteModel)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<City> findAllCities() {
        log.info("DAO: Fetching all cities");
        return cityRepository.findAll()
                .stream()
                .map(routeTransformer::toCityModel)
                .collect(Collectors.toSet());
    }

    @Override
    public Route addPhysicalRoute(String sourceCityId, String destinationCityId, String flightId,
                                  Long departureTimeEpoch, Long arrivalTimeEpoch, Long durationMinutes,
                                  Map<Tier, Integer> unbookedSeatCount,
                                  Map<Tier, Double> price,
                                  Map<Tier, List<Seat>> seatingMap) {
        log.info("DAO: Adding physical route {} -> {} with flightId {}", sourceCityId, destinationCityId, flightId);

        Route routeModel = Route.builder()
                .source(City.builder().id(sourceCityId).build())
                .destination(City.builder().id(destinationCityId).build())
                .flight(Flight.builder().id(flightId).build())
                .departureTimeEpoch(departureTimeEpoch)
                .arrivalTimeEpoch(arrivalTimeEpoch)
                .durationMinutes(durationMinutes)
                .unbookedSeatCount(unbookedSeatCount)
                .price(price)
                .seatingMap(seatingMap)
                .isDirectFlight(true)
                .numberOfStops(0)
                .build();

        PhysicalRouteEntity entity = routeTransformer.toPhysicalEntity(routeModel);
        PhysicalRouteEntity savedEntity = physicalRouteRepository.save(entity);
        return routeTransformer.toPhysicalRouteModel(savedEntity);
    }

}