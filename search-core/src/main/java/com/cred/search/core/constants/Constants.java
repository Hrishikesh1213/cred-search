package com.cred.search.core.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final long MIN_LAYOVER_HOURS = 3600000; // Minimum layover time is 1 hour

    public static final long MAX_LAYOVER_HOURS = 36000000; // Maximum layover time is 10 hour

    public static final int MAX_RESULTS = 10; //Maximum results on a Search

    public static final String SEARCH_SCHEMA = "flight_search";

}
