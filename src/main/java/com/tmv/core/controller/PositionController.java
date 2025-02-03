package com.tmv.core.controller;

import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.dto.PositionDTO;
import com.tmv.core.model.Position;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
@Slf4j
@RestController
class PositionController extends BaseController {

    private final PositionServiceImpl positionService;
    private final MapStructMapper mapper;

    PositionController(PositionServiceImpl positionService, MapStructMapper mapper) {
        this.positionService = positionService;
        this.mapper = mapper;
    }

    @GetMapping("/api/v1/imeis/{imei}/positions/last")
    ResponseEntity<Iterable<PositionDTO>> last(@PathVariable String imei) {
        Iterable<PositionDTO> positions = mapper.toPositionDTO(positionService.findLast(imei));
        return ResponseEntity.ok(positions);
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/api/v1/imeis/{imei}/positions")
    ResponseEntity<Iterable<PositionDTO>> all(@PathVariable String imei, @RequestParam(required = false) Map<String, String> params, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Iterable<PositionDTO> positions = mapper.toPositionDTO(findPositions(imei, params));
        return ResponseEntity.ok(positions);
    }
    // end::get-aggregate-root[]

    @PostMapping("/api/v1/positions")
    @ResponseBody
    ResponseEntity<PositionDTO> getNewPosition(@RequestBody Position newPosition) {
        Position createdPosition =  positionService.newPosition(newPosition);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toPositionDTO(createdPosition));
    }

    // Single item
    @GetMapping("/api/v1/positions/{id}")
    @ResponseBody
    ResponseEntity<PositionDTO> one(@PathVariable Long id) {
        Position position = positionService.findById(id);
        return ResponseEntity.ok(mapper.toPositionDTO(position));
    }

    @PutMapping("/api/v1/positions/{id}")
    @ResponseBody
    ResponseEntity<PositionDTO> replacePosition(@RequestBody Position newPosition, @PathVariable Long id) {
        Position updatedPosition =  positionService.updatePosition(newPosition,id);
        return ResponseEntity.ok(mapper.toPositionDTO(updatedPosition));
    }

    @DeleteMapping("/api/v1/positions/{id}")
    ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    private Iterable<Position> findPositions(String imei, Map<String, String> params) {
        if (params.containsKey("from")) {
            LocalDateTime fromDateTime = MultiFormatDateParser.parseDate(params.get("from"));
            LocalDateTime toDateTime = params.containsKey("to")
                    ? MultiFormatDateParser.parseDate(params.get("to"))
                    : null;
            return positionService.findBetween(imei, fromDateTime, toDateTime);
        }

        if (params.containsKey("to")) {
            LocalDateTime toDateTime = MultiFormatDateParser.parseDate(params.get("to"));
            return positionService.findBetween(imei, null, toDateTime);
        }
        return positionService.findAll(imei);
    }

}
