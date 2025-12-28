package com.cred.search.core.factory;

import com.cred.search.core.exception.SearchServiceException;
import com.cred.search.core.strategy.ISortingStrategy;
import com.cred.search.models.commons.enums.SortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SortingFactory {
    private final Map<SortBy, ISortingStrategy> strategyMap;

    @Autowired
    public SortingFactory(List<ISortingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ISortingStrategy::getSortByType,
                        strategy -> strategy
                ));
    }

    public ISortingStrategy getSortingStrategy(SortBy sortBy) {
        if (Objects.isNull(sortBy)) {
            throw new SearchServiceException("sortBy is null");
        }

        ISortingStrategy strategy = strategyMap.get(sortBy);

        if (Objects.isNull(strategy)) {
            throw new SearchServiceException("No strategy found for " + sortBy);
        }

        return strategy;
    }
}