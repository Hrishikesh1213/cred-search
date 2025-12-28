package com.cred.search.core.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logical_routes", schema = "flight_search")
@SQLRestriction("deleted = false")
public class LogicalRouteEntity extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_city_id", nullable = false)
    private CityEntity source;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private CityEntity destination;

    @Column(name = "start_time_epoch", nullable = false)
    private Long startTimeInEpoch;

    // MAPPING FIX: Explicitly map 'total_duration_millis'
    @Column(name = "total_duration_millis", nullable = false)
    private Long totalDurationInMillis;

    @Column(name = "number_of_stops")
    private Integer numberOfStops;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_legs", columnDefinition = "jsonb")
    private List<Map<String, Object>> routeLegs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_seats_min", columnDefinition = "jsonb")
    private Map<String, Object> availableSeatsMin;

    // NEW: Price Map
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prices", columnDefinition = "jsonb")
    private Map<String, Object> prices;
}