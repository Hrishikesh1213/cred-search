package com.cred.search.core.dao.repository;

import com.cred.search.core.dao.entity.LogicalRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.cred.search.core.constants.Constants.SEARCH_SCHEMA;

@Repository
public interface LogicalRouteRepository extends JpaRepository<LogicalRouteEntity, String> {

    String FIND_AVAILABLE_ROUTES_QUERY =
            "SELECT * FROM " + SEARCH_SCHEMA + ".logical_routes l " +
                    "WHERE l.source_city_id = :source " +
                    "AND l.destination_city_id = :dest " +
                    "AND l.start_time_epoch BETWEEN :start AND :end " +
                    "AND CAST(jsonb_extract_path_text(l.available_seats_min, :tier) AS INTEGER) >= :passengers " +
                    "AND l.deleted = false";

    @Query(value = FIND_AVAILABLE_ROUTES_QUERY, nativeQuery = true)
    List<LogicalRouteEntity> findAvailableRoutes(
            @Param("source") String sourceCode,
            @Param("dest") String destCode,
            @Param("start") Long startEpoch,
            @Param("end") Long endEpoch,
            @Param("tier") String tierName,
            @Param("passengers") Integer passengers
    );
}