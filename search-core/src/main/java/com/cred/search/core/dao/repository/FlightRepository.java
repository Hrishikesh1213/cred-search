package com.cred.search.core.dao.repository;

import com.cred.search.core.dao.entity.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, String> {

}
