package com.tmv.core.service;

import com.tmv.core.model.Journey;
import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.ParkSpot;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface JourneyService {
    Journey createNewJourney(Journey newJourney);
    Optional<Journey> getJourneyById(Long id);
    Journey updateJourney(Long id, Journey newTrack);
    void deleteJourney(Long id);
    Journey startJourney(Long id);
    Journey endJourney(Long id);
    boolean isJourneyActive(Journey journey);
    String determineActiveImei(Journey journey);
    ParkSpot addOvernightParking(Long id,String parkingSpotName, String parkingSpotDescription, boolean createWPPost, LocalDate date) throws IOException;
    OvernightParking updateOvernightParking(Long journeyId, OvernightParking updatedParking);
    LineString trackForJourneyBetween(Journey journeyId, LocalDateTime fromDateTime, LocalDateTime toDateTime, boolean concealLastPosition);
}
