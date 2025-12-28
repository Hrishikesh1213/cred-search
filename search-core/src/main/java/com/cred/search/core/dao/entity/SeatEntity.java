package com.cred.search.core.dao.entity;

import com.cred.search.models.commons.enums.Tier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import static com.cred.search.core.constants.Constants.SEARCH_SCHEMA;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seats", schema = SEARCH_SCHEMA)
@SQLRestriction("deleted = false")
public class SeatEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physical_route_id", nullable = false)
    private PhysicalRouteEntity physicalRoute;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private Tier tier; // ECONOMY, BUSINESS

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "is_booked", nullable = false)
    private boolean isBooked;

    @Column(name = "price", nullable = false)
    private Double price;
}