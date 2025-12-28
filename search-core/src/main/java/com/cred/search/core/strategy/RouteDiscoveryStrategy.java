package com.cred.search.core.strategy;

import com.cred.search.models.commons.City;
import com.cred.search.models.commons.Route;

import java.util.List;
import java.util.Set;

public interface RouteDiscoveryStrategy {

    Set<List<Route>> findValidPaths(Set<Route> allPhysicalRoutes, List<City> allCities);
}