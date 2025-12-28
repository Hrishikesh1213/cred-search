package com.cred.search.core.strategy;

import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.enums.SortBy;
import com.cred.search.models.commons.enums.Tier;

import java.util.List;
import java.util.Set;

public interface ISortingStrategy {

    List<Route> sort(Set<Route> routes, Tier tier, int limit);

    SortBy getSortByType();

}
