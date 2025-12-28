package com.cred.search.core.dao.repository;

import com.cred.search.core.dao.entity.PhysicalRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PhysicalRouteRepository extends JpaRepository<PhysicalRouteEntity, String> {

}
