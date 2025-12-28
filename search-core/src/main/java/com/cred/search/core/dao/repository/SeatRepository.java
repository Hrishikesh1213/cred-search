package com.cred.search.core.dao.repository;

import com.cred.search.core.dao.entity.PhysicalRouteEntity;
import com.cred.search.core.dao.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, String> {

    List<SeatEntity> findAllByPhysicalRoute(PhysicalRouteEntity physicalRoute);

}