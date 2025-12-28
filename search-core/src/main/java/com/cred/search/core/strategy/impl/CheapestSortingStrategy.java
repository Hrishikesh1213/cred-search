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
public class CheapestSortingStrategy implements ISortingStrategy {

    @Override
    public List<Route>  sort(Set<Route> routes, Tier tier, int limit) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }

        if (tier == null) {
            log.error("Cannot sort by Cheapest: Tier is null");
            return List.copyOf(routes);
        }

        return routes.stream()
                .filter(route -> hasPriceForTier(route, tier))
                .sorted(Comparator.comparingDouble(route -> route.getPrice().get(tier)))
                .limit(limit)
                .toList();
    }

    @Override
    public SortBy getSortByType() {
        return SortBy.CHEAPEST;
    }

    private boolean hasPriceForTier(Route route, Tier tier) {
        return route.getPrice() != null
                && route.getPrice().containsKey(tier)
                && route.getPrice().get(tier) != null;
    }
}