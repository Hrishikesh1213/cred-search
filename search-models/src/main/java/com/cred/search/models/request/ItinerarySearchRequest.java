package com.cred.search.models.request;

import com.cred.search.models.commons.enums.SortBy;
import com.cred.search.models.commons.enums.Tier;
import lombok.Data;

import java.util.Date;


@Data
public class ItinerarySearchRequest {

    private String source;

    private String destination;

    private Date travelDate;

    private int numberOfPassengers;

    private Tier tier;

    private SortBy sortBy;

}
