package com.tmv.core.controller;

import com.tmv.core.model.Position;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        Iterable<Position> result = null;

        if (params.containsKey("from")) {
            String fromDate = params.get("from");
            LocalDateTime fromDateTime = MultiFormatDateParser.parseDate(fromDate);
            LocalDateTime toDateTime = null;

            if (params.containsKey("to")) {
                String toDate = params.get("to");
                toDateTime = MultiFormatDateParser.parseDate(toDate);
                result = positionService.findBetween(imei,fromDateTime,toDateTime);
            }
            else {
                result = positionService.findBetween(imei,fromDateTime,null);
            }
        }
        else if (params.containsKey("to")) {
            String toDate = params.get("to");
            LocalDateTime toDateTime = MultiFormatDateParser.parseDate(toDate);
            result = positionService.findBetween(imei,null,toDateTime);
        }
        else {
            result = positionService.findAll(imei);
        }
       return result;
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


}
