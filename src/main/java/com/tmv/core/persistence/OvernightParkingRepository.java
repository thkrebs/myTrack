package com.tmv.core.persistence;

import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.OvernightParkingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OvernightParkingRepository extends JpaRepository<OvernightParking, OvernightParkingId> {
    @Query("SELECT MAX(op.overnightDate) FROM OvernightParking op WHERE op.journey.id = :journeyId AND op.parkSpot.id = :parkSpotId")
    Optional<LocalDate> findLatestParkDate(@Param("journeyId") Long journeyId, @Param("parkSpotId") Long parkSpotId);
}
