package com.tmv.core.controller;

import com.tmv.core.dto.*;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Journey;
import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.ParkSpot;
import com.tmv.core.model.Position;
import com.tmv.core.service.JourneyServiceImpl;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.util.Cache;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tmv.core.util.Distance.calculateDistance;

//TODO intro REST method to mark current position of journey -> use active imeis last position
//     parameter and description (optional)
//     check if a parkSpot already exists in a given radius (radius?)
//     create wordpress doc (only if not yet done) -> need flag in parkSpot

/**
 * The JourneyController class provides a RESTful API interface to manage and retrieve details about journeys,
 * including their tracks, overnight parking locations, and status.
 * It handles HTTP requests related to creating, reading, updating, and deleting journey entities.
 * The controller interacts with a service layer and uses a MapStruct-based mapper for data transformation.
 */
@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class JourneyController extends BaseController {

    private final JourneyServiceImpl journeyService;
    private final PositionServiceImpl positionService;
    private final MapStructMapper mapper;

    @Value("${CONCEALMENT_DISTANCE}")
    private long CONCEALMENT_DISTANCE;

    @Autowired
    private Cache<String, Map<String, Object>> trackCache;

    @Value("${cache.track.liveness}")
    private long CACHE_LIVENESS;

    private final long CACHE_LIVENESS_FOR_COMPLETED_JOURNEY = Integer.MAX_VALUE;


    // cache keys
    private final int ID_CONCEAL = 1;
    private final int ID_FULL = 2;

    JourneyController(@Qualifier("mapStructMapper") MapStructMapper mapstructMapper, JourneyServiceImpl journeyService, PositionServiceImpl positionService) {
        this.journeyService = journeyService;
        this.positionService = positionService;
        this.mapper = mapstructMapper;
    }

    /**
     * Retrieves the current track and associated information of a specified journey as a GeoJSON structure.
     *
     * @param journey the ID of the journey to retrieve the track data for
     * @param params  optional query parameters to filter or customize the track data
     * @return a map representing a GeoJSON object with features including park spots and the route
     */
    @GetMapping(value = "/api/v1/journeys/{journey}/track", produces = "application/json")
    Map<String, Object> currentTrack(@PathVariable Long journey, @RequestParam(required = false) Map<String, String> params) {
        Journey journeyEntity = getValidatedJourney(journey);

        boolean concealTrack = determineConcealment(journeyEntity, params);
        String cacheKey = generateCacheKey(journey, concealTrack ? ID_CONCEAL : ID_FULL);

        if (isCacheValid(cacheKey)) {
            log.info("Cache hit for currentTrack for journey {}", journey);
            return trackCache.get(cacheKey);
        }

        log.info("Cache miss for currentTrack for journey {}", journey);
        Map<String, Object> geoJson = createGeoJsonData(journeyEntity, params, concealTrack);
        trackCache.put(cacheKey, geoJson, CACHE_LIVENESS);

        return geoJson;
    }


        /**
         * Creates a new journey resource based on the provided journey data.
         *
         * @param newJourney the data of the journey to be created, encapsulated in CreateJourneyDTO
         * @return a ResponseEntity containing the created journey encapsulated in JourneyDTO and an HTTP status of CREATED
         */
    @PostMapping(value = "/api/v1/journeys", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<JourneyDTO> createJourney(@RequestBody CreateJourneyDTO newJourney) {
        Journey journey = journeyService.createNewJourney(mapper.toJourneyEntity(newJourney));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toJourneyDTO(journey));
    }

    /**
     * Retrieves a specific journey by its unique identifier.
     *
     * @param id the unique identifier of the journey to be retrieved
     * @return a ResponseEntity containing the JourneyDTO of the specified journey
     * @throws ResourceNotFoundException if no journey with the given id is found
     */
    // Single item
    @GetMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<JourneyDTO> one(@PathVariable Long id) {
        Journey journeyEntity = getValidatedJourney(id);
        return ResponseEntity.ok(mapper.toJourneyDTO(journeyEntity));
    }

    /**
     * Updates an existing journey using the provided journey details and journey ID.
     *
     * @param newJourney The details of the journey to update, encapsulated in a CreateJourneyDTO object.
     * @param id         The ID of the journey to be updated.
     * @return A ResponseEntity containing the updated journey details as a JourneyDTO object.
     */
    @PutMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<JourneyDTO> updateJourney(@RequestBody CreateJourneyDTO newJourney, @PathVariable Long id) {
        Journey updatedJourney = journeyService.updateJourney(id, mapper.toJourneyEntity(newJourney));
        return ResponseEntity.ok(mapper.toJourneyDTO(updatedJourney));
    }

    /**
     * Creates an overnight parking spot for a specific journey.
     *
     * @param journeyId   the ID of the journey for which the overnight parking spot is being created
     * @param name        the name of the parking spot
     * @param description an optional description of the parking spot
     * @return a ResponseEntity containing the created ParkSpotDTO and the HTTP status code
     */
    @PostMapping("/api/v1/journeys/{journeyId}/overnight-parking")
    public ResponseEntity<ParkSpotDTO> createOvernightParkingForJourney(
            @PathVariable Long journeyId,
            @RequestParam(required = true) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) boolean createWPPost,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ParkSpot createdParking = null;
        createdParking = journeyService.addOvernightParking(journeyId, name, description, createWPPost, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toParkSpotDTO(createdParking));
    }

    @GetMapping("/api/v1/journeys/{journeyId}/nearbyParkspots")
    @ResponseBody
    public ResponseEntity<List<ParkSpotDTO>> getNearbyParkingForJourney(@PathVariable Long journeyId, @RequestParam(required = false) Long distance) {

        Journey journeyEntity =  getValidatedJourney(journeyId);
        distance = (distance != null) ? distance : 50L;

        List<ParkSpot> parkspots = journeyService.getNearbyParkSpots(journeyEntity, distance);
        return ResponseEntity.status(HttpStatus.OK).
                body(mapper.toParkSpotDTO(parkspots));
    }

    /**
     * Updates the overnight parking details for a specified journey.
     *
     * @param journeyId         the ID of the journey to update the overnight parking for
     * @param updatedParkingDTO the updated details of the overnight parking
     * @return a ResponseEntity containing the updated OvernightParkingDTO
     */
    @PutMapping("/journeys/{journeyId}/overnight-parking")
    public ResponseEntity<OvernightParkingDTO> updateOvernightParkingForJourney(
            @PathVariable Long journeyId,
            @RequestBody OvernightParkingDTO updatedParkingDTO) {
        OvernightParking updatedOvernightParking = journeyService.updateOvernightParking(journeyId, mapper.toOvernightParkingEntity(updatedParkingDTO));
        return ResponseEntity.ok(mapper.toOvernightParkingDTO(updatedOvernightParking));
    }

    @PutMapping("/api/v1/journeys/{id}/start")
    @ResponseBody
    ResponseEntity<JourneyDTO> startJourney(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Journey updatedJourney = journeyService.startJourney(id);
        return ResponseEntity.ok(mapper.toJourneyDTO(updatedJourney));
    }

    @PutMapping("/api/v1/journeys/{id}/end")
    @ResponseBody
    // @TODO track should be cached finally, for that currentTrack method needs to be re-factored
    ResponseEntity<JourneyDTO> endJourney(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Journey updatedJourney = journeyService.endJourney(id);

        // cache the track; validity can be long
        Journey journeyEntity = getValidatedJourney(id);
        String cacheKey = generateCacheKey(id, ID_FULL);
        trackCache.remove(cacheKey);
        Map<String, Object> geoJson = createGeoJsonData(journeyEntity, new HashMap<>(), false);
        trackCache.put(cacheKey, geoJson, CACHE_LIVENESS_FOR_COMPLETED_JOURNEY);

        return ResponseEntity.ok(mapper.toJourneyDTO(updatedJourney));
    }


    /**
     * Deletes a journey with the specified ID.
     *
     * @param id the unique identifier of the journey to be deleted
     * @return a ResponseEntity with no content to indicate successful deletion
     */
    @DeleteMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<Void> deleteJourney(@PathVariable Long id) {
        journeyService.deleteJourney(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clears the cache for a specific journey.
     *
     * @return A ResponseEntity containing the HTTP status and a message.
     */
    @PostMapping("/api/v1/journeys/clear-cache")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearJourneyCache() {;

        // Remove the cache entries for the specified journey
        trackCache.clear();
        // Prepare the response body
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Cache cleared for all journeys");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the track associated with a specified journey and optional time parameters.
     *
     * @param journeyEntity The unique identifier of the journey whose track is being retrieved.
     * @param params        A map of parameters where optional keys include:
     *                      "from" - The starting date/time of the track range in a supported format.
     *                      "to" - The ending date/time of the track range in a supported format.
     * @return A LineString object representing the track of the journey over the specified range,
     * or the full journey track if no time parameters are specified.
     */
    private LineString getTrack(Journey journeyEntity, Map<String, String> params, boolean concealTrack) {
        // use calcuate date and dispatch to appropriate journeyService
        LocalDateTime fromDateTime = calculateFromDate(journeyEntity, params);
        LocalDateTime toDateTime = calculateEndDate(journeyEntity, params);

        if (fromDateTime != null) { // if no fromDate journey track cannot be retrieved
            return journeyService.trackForJourneyBetween(journeyEntity, fromDateTime, toDateTime, concealTrack);
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createLineString();
    }

    // if params has no startDate use journey startDate
    // if params is specified it is used if within journey date boundaries
    private LocalDateTime calculateFromDate(Journey journeyEntity, Map<String, String> params) {
        LocalDateTime journeyStartDateTime = journeyEntity.getStartDate().atStartOfDay();
        LocalDateTime journeyEndDateTime = journeyEntity.getEndDate().atStartOfDay();

        String fromParam = params.get("from");
        LocalDateTime fromDateTime = journeyStartDateTime; // Standardmäßig auf journeyStartDate setzen

        if (fromParam != null) {
            LocalDateTime fromParamDate = MultiFormatDateParser.parseDate(fromParam); // Parameter in ein LocalDate umwandeln

            // Prüfen, ob der Parameter innerhalb des Start- und Enddatums der Journey liegt
            if ((fromParamDate.isEqual(journeyStartDateTime) || fromParamDate.isAfter(journeyStartDateTime))
                    && (fromParamDate.isEqual(journeyEndDateTime) || fromParamDate.isBefore(journeyEndDateTime))) {
                fromDateTime = fromParamDate; // Das fromParam-Datum verwenden
            }
        }
        return fromDateTime;
    }

    // if params has no endDate use journey endDate
    // if params is specified it is used if within journey date boundaries
    // if no enddates are specified current date is used
    private LocalDateTime calculateEndDate(Journey journeyEntity, Map<String, String> params) {
        // Hole das Start- und Enddatum aus der Journey
        LocalDateTime journeyStartDateTime = journeyEntity.getStartDate().atStartOfDay();
        LocalDateTime journeyEndDateTime = journeyEntity.getEndDate().atStartOfDay();

        // Standardwert ist das aktuelle Datum, falls keine weiteren Werte angegeben sind
        LocalDateTime endDateTime = LocalDateTime.now();

        // Versuch, das "to"-Datum aus den Parametern zu lesen
        String toParam = params.get("to");

        if (journeyEndDateTime != null) {
            // Verwende journey.getEndDate(), falls gesetzt
            endDateTime = journeyEndDateTime;
        }
        if (toParam != null) {
            // Versuche, das "to"-Parameter-Datum zu parsieren
            LocalDateTime toParamDateTime = MultiFormatDateParser.parseDate(toParam);

            // Prüfen, ob das to-Date innerhalb des gültigen Zeitraums liegt
            if ((toParamDateTime.isEqual(journeyStartDateTime) || toParamDateTime.isAfter(journeyStartDateTime))
                    && (toParamDateTime.isEqual(endDateTime) || toParamDateTime.isBefore(endDateTime))) {
                endDateTime = toParamDateTime;
            }
        }
        return endDateTime;
    }

    private List<ParkSpot> filterNearbyParkSpots(List<ParkSpot> parkSpots, Point currentPoint) {
        // Führe Filterung durch
        return parkSpots.stream()
                .filter(parkSpot -> {
                    double distance = calculateDistance(
                            currentPoint.getY(), currentPoint.getX(),                // Aktuelle Position: Lat, Lng
                            parkSpot.getPoint().getY(), parkSpot.getPoint().getX() // Parkspot Position: Lat, Lng
                    );
                    return distance <= 5.0; // Filtere Parkspots innerhalb von 5 km
                })
                .toList(); // Gibt die gefilterte Liste zurück
    }

    /**
     * Generates a unique key for a given journey ID, a long value, and an integer.
     *
     * @param journeyId Journey-ID to be used as part of the key.
     * @param intValue  An integer to encode in the key.
     * @return A unique string that can be used as a cache key.
     */
    private String generateCacheKey(Long journeyId, int intValue) {
        // Generate a unique key based on the inputs
        return String.format("%d-%d", journeyId, intValue);
    }

    private Position getCurrentPosition(Journey journey) {
        String activeIMEI = journeyService.determineActiveImei(journey);
        return positionService.findLast(activeIMEI).iterator().next();
    }
    private List<Map<String, Object>> createFeatures(LineString track, List<ParkSpot> parkSpots,
                                                     Position currentPosition, boolean concealTrack) {
        List<Map<String, Object>> features = new ArrayList<>();
        addParkSpotFeatures(parkSpots, currentPosition, concealTrack, features);
        addRouteFeature(track, features);
        return features;
    }

    private void addParkSpotFeatures(List<ParkSpot> parkSpots, Position currentPosition, boolean concealTrack,
                                     List<Map<String, Object>> features) {
        parkSpots.forEach(parkSpot -> {
            if (concealTrack && currentPosition != null && !isWithinConcealmentDistance(currentPosition, parkSpot)) {
                return; // Skip park spot if too close
            }

            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", List.of(parkSpot.getPoint().getX(), parkSpot.getPoint().getY()));

            Map<String, Object> properties = new HashMap<>();
            properties.put("name", parkSpot.getName() != null ? parkSpot.getName() : "Unnamed");
            properties.put("description", parkSpot.getDescription() != null ? parkSpot.getDescription() : "");

            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            feature.put("properties", properties);

            features.add(feature);
        });
    }

    private boolean isWithinConcealmentDistance(Position currentPosition, ParkSpot parkSpot) {
        double distance = calculateDistance(
                currentPosition.getPoint().getY(), currentPosition.getPoint().getX(),
                parkSpot.getPoint().getY(), parkSpot.getPoint().getX()
        );
        return distance <= CONCEALMENT_DISTANCE;
    }

    private void addRouteFeature(LineString track, List<Map<String, Object>> features) {
        List<List<Double>> coordinates = Arrays.stream(track.getCoordinates())
                .map(coord -> List.of(coord.getX(), coord.getY()))
                .toList();

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Route");
        properties.put("description", "Track of the Journey");

        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");
        feature.put("geometry", geometry);
        feature.put("properties", properties);

        features.add(feature);
    }

    private Map<String, Object> createGeoJsonData(Journey journey, Map<String, String> params, boolean concealTrack) {
        LineString track = getTrack(journey, params, concealTrack);
        List<ParkSpot> parkSpots = journey.getOvernightParkings().stream()
                .map(OvernightParking::getParkSpot)
                .toList();
        Position currentPosition = concealTrack ? getCurrentPosition(journey) : null;

        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", createFeatures(track, parkSpots, currentPosition, concealTrack));

        return geoJson;
    }

    private boolean isCacheValid(String cacheKey) {
        return trackCache.isValid(cacheKey);
    }

    private boolean determineConcealment(Journey journeyEntity, Map<String, String> params) {
        boolean isActive = journeyService.isJourneyActive(journeyEntity);
        String concealParam = params.get("conceal");

        if (concealParam != null) {
            return Boolean.parseBoolean(concealParam);
        }

        return isActive;
    }

    private Journey getValidatedJourney(Long journeyId) {
        return journeyService.getJourneyById(journeyId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));
    }
}
