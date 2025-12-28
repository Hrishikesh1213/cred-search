package com.cred.search.core.manager;

import com.cred.search.models.commons.Route;
import com.cred.search.models.request.ItinerarySearchRequest;
import com.cred.search.models.response.ItinerarySearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ISearchManager {

    ItinerarySearchResponse findFlights(ItinerarySearchRequest request);

    void recalculateRoutes(List<Route> newPhysicalRoutes) throws JsonProcessingException;
}
