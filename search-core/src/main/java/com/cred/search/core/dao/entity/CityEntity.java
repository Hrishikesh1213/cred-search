package com.cred.search.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "cities", schema = SEARCH_SCHEMA)
@SQLRestriction("deleted = false")
public class CityEntity extends BaseEntity {

    @Id
    @Column(name = "code", unique = true, nullable = false, length = 10)
    private String code; // e.g., BOM, DEL (PRIMARY KEY)

    @NotBlank
    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;
}