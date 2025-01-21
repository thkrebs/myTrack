package com.tmv.core.controller;

import com.tmv.core.model.Imei;
import com.tmv.core.model.Position;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.service.ResourceNotFoundException;
import com.tmv.core.util.MultiFormatDateParser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
class PositionController extends BaseController {

    private final PositionServiceImpl positionService;

    PositionController(PositionServiceImpl positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/api/v1/imei/{imei}/positions/last")
    Iterable<Position> last(@PathVariable String imei) {
        return positionService.findLast(imei);
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/api/v1/imei/{imei}/positions")
    Iterable<Position> all(@PathVariable String imei, @RequestParam(required = false) Map<String, String> params, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return findPositions(imei, params);
    }
    // end::get-aggregate-root[]

    @PostMapping("/api/v1/positions")
    @ResponseBody
    Position getNewPosition(@RequestBody Position newPosition) {
        return positionService.newPosition(newPosition);
    }

    // Single item
    @GetMapping("/api/v1/position/{id}")
    @ResponseBody
    Position one(@PathVariable Long id) {
        return positionService.findById(id);
    }

    @PutMapping("/api/v1/position/{id}")
    @ResponseBody
    Position replacePosition(@RequestBody Position newPosition, @PathVariable Long id) {
        return positionService.updatePosition(newPosition,id);
    }

    @DeleteMapping("/api/v1/position/{id}")
    void deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
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
