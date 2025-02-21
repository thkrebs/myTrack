package com.tmv.core.persistence;

import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.OvernightParkingId;
import com.tmv.core.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OvernightParkingRepository extends JpaRepository<OvernightParking, OvernightParkingId> {

}
