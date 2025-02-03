package com.tmv.core.persistence;

import com.tmv.core.model.ParkSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkSpotRepository  extends JpaRepository<ParkSpot, Long>  {
    @Query(value = "SELECT * FROM parkspot WHERE ST_DWithin(point, ST_SetSRID(ST_MakePoint(?1, ?2), 4326), ?3)", nativeQuery = true)
    List<ParkSpot> findWithinDistance(double longitude, double latitude, double distanceInMeters);

}
