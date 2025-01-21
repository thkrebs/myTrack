package com.tmv.core.service;

import com.tmv.core.model.Journey;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JourneyService {
    LineString trackForJourney(Long journeyId);
    Journey createNewJourney(Journey newJourney);
    Optional<Journey> getJourneyById(Long id);
    Journey updateJourney(Long id, Journey newTrack);
    void deleteJourney(Long id);
    LineString trackForJourneyBetween(Long journeyId, LocalDateTime fromDateTime, LocalDateTime toDateTime);
}
