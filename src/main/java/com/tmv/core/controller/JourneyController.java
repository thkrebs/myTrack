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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

//TODO intro REST method to mark current position of journey -> use active imeis last position
//     parameter and description (optional)
//     check if a parkSpot already exists in a given radius (radius?)
//     create wordpress doc (only if not yet done) -> need flag in parkSpot

@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class JourneyController extends BaseController {

    private final JourneyServiceImpl journeyService;
    private final MapStructMapper mapper;

    JourneyController(MapStructMapper mapstructMapper, JourneyServiceImpl journeyService) {
        this.journeyService = journeyService;
        this.mapper = mapstructMapper;
    }

    @GetMapping(value="/api/v1/journeys/{journey}/track/", produces = "application/json")
    LineString currentTrack(@PathVariable Long journey, @RequestParam(required = false) Map<String, String> params) {
        return getTrack(journey, params);
    }


    @PostMapping(value="/api/v1/journeys", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<JourneyDTO> createJourney(@RequestBody CreateJourneyDTO newJourney) {
        Journey journey = journeyService.createNewJourney(mapper.toJourneyEntity(newJourney));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toJourneyDTO(journey));
    }

    // Single item
    @GetMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<JourneyDTO> one(@PathVariable Long id) {
        Journey journey = journeyService.getJourneyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
        return ResponseEntity.ok(mapper.toJourneyDTO(journey));
    }

    @PutMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<JourneyDTO> updateJourney(@RequestBody CreateJourneyDTO newJourney, @PathVariable Long id) {
        Journey updatedJourney = journeyService.updateJourney(id,mapper.toJourneyEntity(newJourney));
        return ResponseEntity.ok(mapper.toJourneyDTO(updatedJourney));
    }

    @PostMapping("/api/v1/journeys/{journeyId}/overnight-parking")
    public ResponseEntity<ParkSpotDTO> createOvernightParkingForJourney(
            @PathVariable Long journeyId,
            @RequestParam(required = true) String name,
            @RequestParam(required = false) String description) {
        ParkSpot createdParking = journeyService.addOvernightParking(journeyId, name, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toParkSpotDTO(createdParking));
    }

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


    @DeleteMapping("/api/v1/journeys/{id}")
    @ResponseBody
    ResponseEntity<Void> deleteJourney(@PathVariable Long id) {
        journeyService.deleteJourney(id);
        return ResponseEntity.noContent().build();
    }


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
