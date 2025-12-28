package com.cred.search.models.response;

import com.cred.search.models.commons.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarySearchResponse {

    private List<Route> itineraryList;

}