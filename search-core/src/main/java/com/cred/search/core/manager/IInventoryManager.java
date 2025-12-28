package com.cred.search.core.manager;

import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;
import com.cred.search.models.request.PhysicalRouteRequest;

import java.util.Set;

public interface IInventoryManager {

    Set<Route> findAllPhysicalRoutes();

    Set<City> getAllCities();

    Route addPhysicalRoute(PhysicalRouteRequest route);

}
