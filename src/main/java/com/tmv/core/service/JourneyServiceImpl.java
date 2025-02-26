package com.tmv.core.service;

import com.tmv.core.dto.OvernightParkingDTO;
import com.tmv.core.exception.ConstraintViolationException;
import com.tmv.core.exception.ResourceAlreadyExistsException;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.*;
import com.tmv.core.persistence.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JourneyServiceImpl implements JourneyService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PositionRepository positionRepository;
    private final GeometryFactory geomFactory;
    private final JourneyRepository journeyRepository;
    private final ParkSpotRepository parkSpotRepository;
    private final OvernightParkingRepository overnightParkingRepository;
    private final ImeiRepository imeiRepository;

    JourneyServiceImpl(PositionRepository positionRepository, JourneyRepository journeyRepository, ParkSpotRepository parkSpotRepository, OvernightParkingRepository overnightParkingRepository, ImeiRepository imeiRepository) {
        super();
        this.geomFactory = new GeometryFactory();
        this.positionRepository = positionRepository;
        this.journeyRepository = journeyRepository;
        this.parkSpotRepository = parkSpotRepository;
        this.overnightParkingRepository = overnightParkingRepository;
        this.imeiRepository = imeiRepository;
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
            List<Position> positions = positionRepository.findByImeiInAndDateTimeBetweenOrderByDateTimeAsc(imeiStrings, fromDateTime, toDateTime);
            return createLineStringFromPositions(positions);
        } else if (toDateTime == null) {
            List<Position> positions = positionRepository.findByImeiInAndDateTimeGreaterThanEqualOrderByDateTimeAsc(imeiStrings, fromDateTime);
            return createLineStringFromPositions(positions);
        } else {
            List<Position> positions = positionRepository.findByImeiInAndDateTimeLessThanEqualOrderByDateTimeAsc(imeiStrings, toDateTime);
            return createLineStringFromPositions(positions);
        }
    }

    public Journey createNewJourney(Journey newJourney) {
        log.info("Creating a new journey: {}", newJourney);
        processTrackImeis(newJourney);
        return journeyRepository.save(newJourney);
    }

    public Optional<Journey> getJourneyById(Long id) {
        return journeyRepository.findById(id);
    }

    @Override
    public Journey updateJourney(Long id, Journey newJourney) {
        processTrackImeis(newJourney);
        return journeyRepository.findById(id)
                .map(existingJourney -> {
                    existingJourney.setDescription(newJourney.getDescription());
                    existingJourney.setStartDate(newJourney.getStartDate());
                    existingJourney.setEndDate(newJourney.getEndDate());
                    existingJourney.setTrackedByImeis(newJourney.getTrackedByImeis());
                    existingJourney.setName(newJourney.getName());
                    return journeyRepository.save(existingJourney);
                })
                .orElseGet(() -> {
                    newJourney.setId(id);
                    return journeyRepository.save(newJourney);
                });
    }

    public Journey startJourney(Long id) {
        Journey journey = journeyRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
        if (!isAnyImeiActive(journey)) {
            throw new ConstraintViolationException("Cannot start journey. No tracker is active");
        }
        journey.setStartDate(LocalDate.now());
        return journeyRepository.save(journey);
    }

    public Journey endJourney(Long id) {
        Journey journey = journeyRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
        // do not need an active tracker to end
        journey.setEndDate(LocalDate.now());
        return journeyRepository.save(journey);
    }

    @Transactional
    public ParkSpot addOvernightParking(Long id, String parkingSpotName, String parkingSpotDescription) {
        Journey journey = journeyRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
        Position lastPosition = getLastPositionForActiveImei(journey);
        // check whether there is already a position in proximity
        List<ParkSpot> spotsNearBy = parkSpotRepository.findWithinDistance(
                lastPosition.getPoint().getX(),
                lastPosition.getPoint().getY(),
                50);
        log.info("Found {} park spots. Will take the first one", spotsNearBy.size());
        if (spotsNearBy.isEmpty()) {
            return createNewParkSpotForJourney(journey, lastPosition, parkingSpotName, parkingSpotDescription);
        } else {
            return addOvernightParkingForJourney(journey, spotsNearBy.getFirst());
        }

    }

    public OvernightParking updateOvernightParking(Long journeyId, OvernightParking updatedParking) {
        return overnightParkingRepository.findById(updatedParking.getId())
                .map(existingOvernightParking -> {
                    existingOvernightParking.setOvernightDate(updatedParking.getOvernightDate());
                    existingOvernightParking.setParkSpot(updatedParking.getParkSpot());
                    return overnightParkingRepository.save(existingOvernightParking);
                }).orElseThrow(() ->
                        new ResourceNotFoundException("Overnight Parking with journey id: "
                                + updatedParking.getId().getJourneyId() + " parkspot id: "
                                + updatedParking.getId().getParkSpotId()
                        ));
    }

    protected ParkSpot createNewParkSpotForJourney(Journey journey, Position position, String parkingSpotName, String parkingSpotDescription) {
        OvernightParking overnightParking = new OvernightParking();
        overnightParking.setOvernightDate(position.getDateTime().toLocalDate());

        ParkSpot parkSpot = new ParkSpot();
        parkSpot.setPoint(position.getPoint());
        parkSpot.setName(parkingSpotName);
        parkSpot.setDescription(parkingSpotDescription);
        parkSpotRepository.save(parkSpot);

        overnightParking.setJourney(journey);
        overnightParking.setParkSpot(parkSpot);

        parkSpot.getOvernightParkings().add(overnightParking);
        journey.getOvernightParkings().add(overnightParking);
        //overnightParkingRepository.save(overnightParking);
        journeyRepository.save(journey);

        return parkSpot;
    }

    protected ParkSpot addOvernightParkingForJourney(Journey journey, ParkSpot parkSpot) {
        OvernightParkingId overnightParkingId = new OvernightParkingId();
        overnightParkingId.setOvernightDate(LocalDate.now());
        overnightParkingId.setJourneyId(journey.getId());
        overnightParkingId.setParkSpotId(parkSpot.getId());
        Optional<OvernightParking> optional = overnightParkingRepository.findById(overnightParkingId);
        if (optional.isPresent()) {
            throw new ResourceAlreadyExistsException("Overnight Parking with ID " + overnightParkingId + " already exists.");
        }

        OvernightParking overnightParking = new OvernightParking();
        overnightParking.setId(overnightParkingId);

        //overnightParking.setOvernightDate(LocalDate.now());
        overnightParking.setJourney(journey);
        overnightParking.setParkSpot(parkSpot);

        parkSpot.getOvernightParkings().add(overnightParking);
        journey.getOvernightParkings().add(overnightParking);

        overnightParkingRepository.save(overnightParking);
        journeyRepository.save(journey);
        parkSpotRepository.save(parkSpot);
        return parkSpot;
    }

    private Position getLastPositionForActiveImei(Journey journey) {
        Imei activeImei = getActiveImei(journey);
        if (activeImei == null) {
            throw new ConstraintViolationException("Could not find any active tracker for journey with id:" + journey.getId());
        }
        Iterator<Position> iterator = positionRepository.findTopByImeiOrderByDateTimeDesc(activeImei.getImei()).iterator();
        if (!iterator.hasNext()) {
            throw new ResourceNotFoundException("No positions found for active tracker with IMEI: " + activeImei.getImei());
        }
        return iterator.next();
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

    private boolean isAnyImeiActive(Journey journey) {
        return journey.getTrackedByImeis()
                .stream()
                .anyMatch(Imei::isActive);
    }

    private Imei getActiveImei(Journey journey) {
        return journey.getTrackedByImeis()
                .stream()
                .filter(Imei::isActive)
                .findFirst()
                .orElse(null);
    }

    private void processTrackImeis(Journey newJourney) {
        List<Imei> imeis = newJourney.getTrackedByImeis().stream().toList();
        imeis.forEach(providedImei -> {
            Optional<Imei> imei = Optional.ofNullable(imeiRepository.findByImei(providedImei.getImei()));
            imei.ifPresent(persistedImei -> newJourney.setId(persistedImei.getId()));
        });
    }
}
