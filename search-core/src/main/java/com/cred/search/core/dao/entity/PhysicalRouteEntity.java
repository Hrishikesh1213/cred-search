package com.cred.search.core.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

import static com.cred.search.core.constants.Constants.SEARCH_SCHEMA;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "physical_routes", schema = SEARCH_SCHEMA)
@SQLRestriction("deleted = false")
public class PhysicalRouteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_city_id", nullable = false)
    private CityEntity source;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private CityEntity destination;

    @Column(name = "start_time_epoch", nullable = false)
    private Long startTimeInEpoch;

    @Column(name = "duration_millis", nullable = false)
    private Long durationInMillis;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", nullable = false)
    private FlightEntity flight;

    // The single source of truth for inventory.
    // FetchType.LAZY is recommended if you have 200+ seats to avoid loading them when not needed.
    @OneToMany(mappedBy = "physicalRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatEntity> seats;
}