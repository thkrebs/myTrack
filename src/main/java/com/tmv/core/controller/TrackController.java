package com.tmv.core.controller;

import com.tmv.core.model.Track;
import com.tmv.core.service.TrackService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class TrackController extends BaseController {

    private final TrackService trackService;

    TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping(value="/api/v1/imei/{imei}/tracks/", produces = "application/json")
    LineString currentTrack(@PathVariable String imei) {
        return trackService.currentTrack(imei);
    }
}
