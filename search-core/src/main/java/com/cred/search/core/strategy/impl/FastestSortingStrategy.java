package com.cred.search.core.strategy.impl;

import com.cred.search.core.strategy.ISortingStrategy;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.enums.SortBy;
import com.cred.search.models.commons.enums.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class FastestSortingStrategy implements ISortingStrategy {

    @Override
    public List<Route> sort(Set<Route> routes, Tier tier, int limit) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }

        return routes.stream()
                .filter(route -> route.getDurationMinutes() != null)
                .filter(route -> isTierValid(route, tier))
                .sorted(Comparator.comparingLong(Route::getDurationMinutes))
                .limit(limit)
                .toList();
    }

    @Override
    public SortBy getSortByType() {
        return SortBy.FASTEST;
    }

    private boolean isTierValid(Route route, Tier tier) {
        if (tier == null) return true;
        return route.getPrice() != null && route.getPrice().containsKey(tier);
    }
}