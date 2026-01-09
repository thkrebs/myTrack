package com.tmv.core.controller;

import com.tmv.core.dto.ParkSpotDTO;
import com.tmv.core.service.ParkSpotService;
import com.tmv.core.dto.MapStructMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/parkspots")
public class ParkSpotController extends BaseController {

    private final ParkSpotService parkSpotService;
    private final MapStructMapper mapper;

    @Autowired
    public ParkSpotController(ParkSpotService parkSpotService, MapStructMapper mapper) {
        this.parkSpotService = parkSpotService;
        this.mapper = mapper;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ParkSpotDTO>> getNearbyParkSpots(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "1000") double distance) {
        List<ParkSpotDTO> parkSpots = parkSpotService.findParkSpotsWithinDistance(longitude, latitude, distance).stream()
                .map(mapper::toParkSpotDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(parkSpots);
    }

    @GetMapping("/{id}/debug")
    @PreAuthorize("hasRole('GOD')")
    public ResponseEntity<Map<String, Object>> debugParkSpot(@PathVariable Long id) {
        return ResponseEntity.ok(parkSpotService.debugParkSpot(id));
    }
}
