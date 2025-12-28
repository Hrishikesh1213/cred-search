package com.cred.search.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "flights", schema = SEARCH_SCHEMA)
@SQLRestriction("deleted = false")
public class FlightEntity extends BaseEntity {

    @Id
    @NotBlank
    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber; // e.g., AI-101 (PRIMARY KEY)

    @NotBlank
    @Column(name = "operator", nullable = false, length = 255)
    private String operator;

    @NotNull
    @Column(name = "capacity")
    private Integer capacity;
}