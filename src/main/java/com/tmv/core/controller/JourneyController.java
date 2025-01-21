package com.tmv.core.controller;

import com.tmv.core.model.Journey;
import com.tmv.core.model.Position;
import com.tmv.core.service.JourneyServiceImpl;
import com.tmv.core.service.ResourceNotFoundException;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class JourneyController extends BaseController {

    private final JourneyServiceImpl journeyService;

    JourneyController(JourneyServiceImpl journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping(value="/api/v1/journey/{journey}/track/", produces = "application/json")
    LineString currentTrack(@PathVariable Long journey, @RequestParam(required = false) Map<String, String> params) {
        return getTrack(journey, params);
    }


    @PostMapping("/api/v1/journeys")
    @ResponseBody
    Journey createJourney(@RequestBody Journey newJourney) {
        return journeyService.createNewJourney(newJourney);
    }

    // Single item
    @GetMapping("/api/v1/journey/{id}")
    @ResponseBody
    Journey one(@PathVariable Long id) {
        return journeyService.getJourneyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));
    }

    @PutMapping("/api/v1/journey/{id}")
    @ResponseBody
    Journey updateJourney(@RequestBody Journey newJourney, @PathVariable Long id) {
        return journeyService.updateJourney(id,newJourney);
    }

    @DeleteMapping("/api/v1/journey/{id}")
    @ResponseBody
    void deleteJourney(@PathVariable Long id) {
        journeyService.deleteJourney(id);
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
