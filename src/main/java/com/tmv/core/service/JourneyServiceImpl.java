package com.tmv.core.service;

import com.tmv.core.model.Imei;
import com.tmv.core.model.Journey;
import com.tmv.core.model.Position;
import com.tmv.core.persistence.JourneyRepository;
import com.tmv.core.persistence.PositionRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JourneyServiceImpl implements JourneyService {

    private final PositionRepository positionRepository;
    private final GeometryFactory geomFactory;
    private final JourneyRepository journeyRepository;

    JourneyServiceImpl(PositionRepository positionRepository, JourneyRepository journeyRepository) {
        super();
        this.geomFactory = new GeometryFactory();
        this.positionRepository = positionRepository;
        this.journeyRepository = journeyRepository;
    }

    public LineString trackForJourney(Long journeyId) {
        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));

        List<String> imeiStrings = extractImeiStrings(journey);
        List<Position> positions = positionRepository.findByImeiInOrderByDateTimeAsc(imeiStrings);

        return createLineStringFromPositions(positions);
    }

    public LineString trackForJourneyBetween(Long journeyId, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));

        List<String> imeiStrings = extractImeiStrings(journey);
        if ((fromDateTime != null) && (toDateTime != null)) {
            List<Position> positions =  positionRepository.findByImeiInAndDateTimeBetweenOrderByDateTimeAsc(imeiStrings,fromDateTime, toDateTime);
            return createLineStringFromPositions(positions);
        }
        else if (toDateTime == null) {
            List<Position> positions = positionRepository.findByImeiInAndDateTimeGreaterThanEqualOrderByDateTimeAsc(imeiStrings,fromDateTime);
            return createLineStringFromPositions(positions);
        }
        else  {
            List<Position> positions = positionRepository.findByImeiInAndDateTimeLessThanEqualOrderByDateTimeAsc(imeiStrings, toDateTime);
            return createLineStringFromPositions(positions);
        }
    }

    public Journey createNewJourney(Journey newJourney) {
        log.info("Creating a new journey: {}", newJourney);
        return journeyRepository.save(newJourney);
    }

    public Optional<Journey> getJourneyById(Long id) {
        return journeyRepository.findById(id);
    }

    @Override
    public Journey updateJourney(Long id, Journey newTrack) {
        return journeyRepository.findById(id)
                .map(existingJourney -> {
                    existingJourney.setDescription(newTrack.getDescription());
                    existingJourney.setStartDate(newTrack.getStartDate());
                    existingJourney.setEndDate(newTrack.getEndDate());
                    existingJourney.setTrackedByImeis(newTrack.getTrackedByImeis());
                    return journeyRepository.save(existingJourney);
                })
                .orElseGet(() -> {
                    newTrack.setId(id);
                    return journeyRepository.save(newTrack);
                });
    }

    public void deleteJourney(Long id) {
        if (!journeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Journey not found with id: " + id);
        }
        journeyRepository.deleteById(id);
    }

    private List<String> extractImeiStrings(Journey journey) {
        return journey.getTrackedByImeis()
                .stream()
                .map(Imei::getImei)
                .collect(Collectors.toList());
    }

    private LineString createLineStringFromPositions(List<Position> positions) {
        Coordinate[] coordinates = positions.stream()
                .map(this::mapToCoordinate)
                .toArray(Coordinate[]::new);
        return geomFactory.createLineString(coordinates);
    }

    private Coordinate mapToCoordinate(Position position) {
        return new Coordinate(position.getPoint().getX(), position.getPoint().getY());
    }
}
