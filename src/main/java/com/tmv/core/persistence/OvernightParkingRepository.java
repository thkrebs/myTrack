package com.tmv.core.persistence;

import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.OvernightParkingId;
import com.tmv.core.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OvernightParkingRepository extends JpaRepository<OvernightParking, Long> {
    OvernightParking findById(OvernightParkingId id);

}
