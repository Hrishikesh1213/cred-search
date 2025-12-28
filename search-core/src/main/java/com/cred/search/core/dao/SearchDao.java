package com.cred.search.core.dao;


import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.enums.Tier;

import java.util.Date;
import java.util.Set;

public interface SearchDao {

    Set<Route> findFlights(String source, String destination, Date travelDate, int numberOfPassengers, Tier tier);

    void syncWithDb(Set<Route> finalRoutes);
}
