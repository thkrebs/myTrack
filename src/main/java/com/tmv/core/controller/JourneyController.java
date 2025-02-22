package com.tmv.core.controller;

import com.tmv.core.dto.*;
import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Journey;
import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.ParkSpot;
import com.tmv.core.service.JourneyServiceImpl;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private final MapStructMapper mapper;

    JourneyController(@Qualifier("mapStructMapper") MapStructMapper mapstructMapper, JourneyServiceImpl journeyService) {
        this.journeyService = journeyService;
        this.mapper = mapstructMapper;
    }

    /**
     * Retrieves the current track and associated information of a specified journey as a GeoJSON structure.
     *
     * @param journey the ID of the journey to retrieve the track data for
     * @param params optional query parameters to filter or customize the track data
     * @return a map representing a GeoJSON object with features including park spots and the route
     */
    @GetMapping(value = "/api/v1/journeys/{journey}/track", produces = "application/json")
    Map<String, Object> currentTrack(@PathVariable Long journey, @RequestParam(required = false) Map<String, String> params) {
        // get route
        LineString track = getTrack(journey, params);

        // get all parkspots
        Journey journeyEntity = journeyService.getJourneyById(journey)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journey));
        List<ParkSpot> parkSpots = journeyEntity.getOvernightParkings().stream()
                .map(OvernightParking::getParkSpot)
                .toList();

        // Create mutable GeoJson object
        Map<String, Object> geoJson = new java.util.HashMap<>();
        geoJson.put("type", "FeatureCollection");

        // Create mutable features list
        List<Map<String, Object>> features = new ArrayList<>();

        // Populate features with parkSpots, if any
        if (!parkSpots.isEmpty()) {
            parkSpots.forEach(parkSpot -> {
                Map<String, Object> geometry = new java.util.HashMap<>();
                geometry.put("type", "Point");
                geometry.put("coordinates", List.of(parkSpot.getPoint().getX(), parkSpot.getPoint().getY()));

                Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("name", parkSpot.getName() != null ? parkSpot.getName() : "Unnamed");
                properties.put("description", parkSpot.getDescription() != null ? parkSpot.getDescription() : " ");

                Map<String, Object> feature = new java.util.HashMap<>();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", properties);

                features.add(feature);
            });
        }

        // Extract and filter x, y coordinates
        List<List<Double>> filteredCoordinates = new ArrayList<>();
        Arrays.stream(track.getCoordinates()).forEach(coordinate ->
                filteredCoordinates.add(List.of(coordinate.getX(), coordinate.getY()))
        );

        // Insert route as the final feature
        Map<String, Object> routeGeometry = new java.util.HashMap<>();
        routeGeometry.put("type", "LineString");
        routeGeometry.put("coordinates", filteredCoordinates);

        Map<String, Object> routeProperties = new java.util.HashMap<>();
        routeProperties.put("name", journeyEntity.getName() != null ? journeyEntity.getName() : "Unnamed");
        routeProperties.put("description", journeyEntity.getDescription() != null ? journeyEntity.getDescription() : " ");

        Map<String, Object> routeFeature = new java.util.HashMap<>();
        routeFeature.put("type", "Feature");
        routeFeature.put("geometry", routeGeometry);
        routeFeature.put("properties", routeProperties);

        features.add(routeFeature);

        // Add features to geoJson
        geoJson.put("features", features);

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
        Journey journey = journeyService.getJourneyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
        return ResponseEntity.ok(mapper.toJourneyDTO(journey));
    }

    /**
     * Updates an existing journey using the provided journey details and journey ID.
     *
     * @param newJourney The details of the journey to update, encapsulated in a CreateJourneyDTO object.
     * @param id The ID of the journey to be updated.
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
     * @param journeyId the ID of the journey for which the overnight parking spot is being created
     * @param name the name of the parking spot
     * @param description an optional description of the parking spot
     * @return a ResponseEntity containing the created ParkSpotDTO and the HTTP status code
     */
    @PostMapping("/api/v1/journeys/{journeyId}/overnight-parking")
    public ResponseEntity<ParkSpotDTO> createOvernightParkingForJourney(
            @PathVariable Long journeyId,
            @RequestParam(required = true) String name,
            @RequestParam(required = false) String description) {
        ParkSpot createdParking = journeyService.addOvernightParking(journeyId, name, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toParkSpotDTO(createdParking));
    }

    /**
     * Updates the overnight parking details for a specified journey.
     *
     * @param journeyId the ID of the journey to update the overnight parking for
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
        Journey updatedJourney = journeyService.startJourney(id);
        return ResponseEntity.ok(mapper.toJourneyDTO(updatedJourney));
    }

    @PutMapping("/api/v1/journeys/{id}/end")
    @ResponseBody
    ResponseEntity<JourneyDTO> endJourney(@PathVariable Long id) {
        Journey updatedJourney = journeyService.endJourney(id);
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
     * Retrieves the track associated with a specified journey and optional time parameters.
     *
     * @param journeyId The unique identifier of the journey whose track is being retrieved.
     * @param params A map of parameters where optional keys include:
     *               "from" - The starting date/time of the track range in a supported format.
     *               "to" - The ending date/time of the track range in a supported format.
     * @return A LineString object representing the track of the journey over the specified range,
     *         or the full journey track if no time parameters are specified.
     */
    private LineString getTrack(Long journeyId, Map<String, String> params) {
        if (params.containsKey("from")) {
            LocalDateTime fromDateTime = MultiFormatDateParser.parseDate(params.get("from"));
            LocalDateTime toDateTime = params.containsKey("to")
                    ? MultiFormatDateParser.parseDate(params.get("to"))
                    : null;
            return journeyService.trackForJourneyBetween(journeyId, fromDateTime, toDateTime);
        }

        if (params.containsKey("to")) {
            LocalDateTime toDateTime = MultiFormatDateParser.parseDate(params.get("to"));
            return journeyService.trackForJourneyBetween(journeyId, null, toDateTime);
        }
        return journeyService.trackForJourney(journeyId);
    }

}
