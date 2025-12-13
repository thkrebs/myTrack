package com.tmv.core.persistence;


import com.tmv.core.model.Journey;
import com.tmv.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long> {

    @Query("SELECT j FROM Journey j WHERE j.owner.id = :ownerId AND j.startDate IS NOT NULL AND j.endDate IS NULL ORDER BY j.startDate DESC")
    List<Journey> findActiveJourneysByOwner(@Param("ownerId") Long ownerId);

    List<Journey> findByOwner(User user);
}
