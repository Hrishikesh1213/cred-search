package com.cred.search.core.manager.impl;

import com.cred.search.core.dao.SearchDao;
import com.cred.search.core.factory.SortingFactory;
import com.cred.search.core.manager.IInventoryManager;
import com.cred.search.core.manager.ISearchManager;
import com.cred.search.core.strategy.RouteDiscoveryStrategy;
import com.cred.search.core.transformer.RouteTransformer;
import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;
import com.cred.search.models.request.ItinerarySearchRequest;
import com.cred.search.models.response.ItinerarySearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cred.search.core.constants.Constants.MAX_RESULTS;

@Slf4j
@Component
public class SearchManagerImpl implements ISearchManager {

    private final SearchDao searchDao;
    private final IInventoryManager inventoryManager;
    private final SortingFactory sortingFactory;
    private final RouteTransformer routeTransformer;
    private final RouteDiscoveryStrategy routeDiscoveryStrategy;

    @Autowired
    public SearchManagerImpl(
            SearchDao searchDao,
            IInventoryManager inventoryManager,
            SortingFactory sortingFactory,
            RouteTransformer routeTransformer,
            @Qualifier("bfsRouteStrategy") RouteDiscoveryStrategy routeDiscoveryStrategy
    ) {
        this.searchDao = searchDao;
        this.inventoryManager = inventoryManager;
        this.sortingFactory = sortingFactory;
        this.routeTransformer = routeTransformer;
        this.routeDiscoveryStrategy = routeDiscoveryStrategy;
    }

    @Override
    public ItinerarySearchResponse findFlights(ItinerarySearchRequest request) {
        Set<Route> routes = searchDao.findFlights(
                request.getSource(), request.getDestination(),
                request.getTravelDate(), request.getNumberOfPassengers(), request.getTier()
        );

        List<Route> sortedRoutes = sortingFactory.getSortingStrategy(request.getSortBy())
                .sort(routes, request.getTier(), MAX_RESULTS);

        return ItinerarySearchResponse.builder()
                .itineraryList(sortedRoutes)
                .build();
    }

    @Override
    public void recalculateRoutes(List<Route> newPhysicalRoutes) throws JsonProcessingException {
        Set<Route> allInventory = inventoryManager.findAllPhysicalRoutes();
        allInventory.addAll(newPhysicalRoutes);
        List<City> allCities = new ArrayList<>(inventoryManager.getAllCities());

        log.info("Starting route recalculation for {} physical routes and {} cities",
                allInventory.size(), allCities.size());

        Set<List<Route>> rawPaths = routeDiscoveryStrategy.findValidPaths(allInventory, allCities);

        Set<Route> finalLogicalRoutes = new HashSet<>();
        for (List<Route> path : rawPaths) {
            City origin = path.get(0).getSource();
            City dest = path.get(path.size() - 1).getDestination();

            Route mergedRoute = routeTransformer.mergeRoute(new ArrayList<>(path), origin, dest);
            finalLogicalRoutes.add(mergedRoute);
        }

        searchDao.syncWithDb(finalLogicalRoutes);
        log.info("Route recalculation completed. Synced {} routes.", finalLogicalRoutes.size());
    }
}