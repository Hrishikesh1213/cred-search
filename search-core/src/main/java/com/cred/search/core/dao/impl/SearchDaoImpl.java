package com.cred.search.core.dao.impl;

import com.cred.search.core.dao.SearchDao;
import com.cred.search.core.dao.entity.LogicalRouteEntity;
import com.cred.search.core.dao.repository.LogicalRouteRepository;
import com.cred.search.core.transformer.RouteTransformer;
import com.cred.search.models.commons.Route;
import com.cred.search.models.commons.enums.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class SearchDaoImpl implements SearchDao {

    private final LogicalRouteRepository logicalRouteRepository;
    private final RouteTransformer routeTransformer;


    @Override
    public Set<Route> findFlights(String source, String destination, Date travelDate, int numberOfPassengers, Tier tier) {
        log.info("DAO: Searching flights {} -> {} on {}", source, destination, travelDate);

        LocalDate localDate = travelDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long startEpoch = localDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long endEpoch = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1;

        List<LogicalRouteEntity> entities = logicalRouteRepository.findAvailableRoutes(
                source,
                destination,
                startEpoch,
                endEpoch,
                tier.name(),
                numberOfPassengers
        );

        return entities.stream()
                .map(routeTransformer::toRouteModel)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void syncWithDb(Set<Route> finalRoutes) {

        Set<LogicalRouteEntity> entitiesFromDb = new HashSet<>(logicalRouteRepository.findAll());
        Set<LogicalRouteEntity> finalRouteEntities = finalRoutes.stream()
                .map(routeTransformer::toLogicalEntity)
                .collect(Collectors.toSet());

        Set<String> dbIds = entitiesFromDb.stream()
                .map(LogicalRouteEntity::getId)
                .collect(Collectors.toSet());

        Set<String> finalIds = finalRouteEntities.stream()
                .map(LogicalRouteEntity::getId)
                .collect(Collectors.toSet());

        // DELETE (In DB, but NOT in new list)
        List<LogicalRouteEntity> toDelete = entitiesFromDb.stream()
                .filter(e -> !finalIds.contains(e.getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            logicalRouteRepository.deleteAll(toDelete);
            log.info("Sync: Deleted {} obsolete routes.", toDelete.size());
        }

        // CREATE (In new list, but NOT in DB)
        List<LogicalRouteEntity> toAdd = finalRouteEntities.stream()
                .filter(e -> !dbIds.contains(e.getId()))
                .toList();

        if (!toAdd.isEmpty()) {
            logicalRouteRepository.saveAll(toAdd);
            log.info("Sync: Added {} new routes.", toAdd.size());
        }
    }
}