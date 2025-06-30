package com.tmv.core.controller;

import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.dto.PositionDTO;
import com.tmv.core.model.Position;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.util.MultiFormatDateParser;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
/**
 * PositionController is a REST controller that provides endpoints for managing and retrieving Position entities.
 * It utilizes PositionServiceImpl for business logic and MapStructMapper for mapping entities to DTOs.
 *
 * The controller includes endpoints to:
 * - Retrieve the latest position for a specific IMEI.
 * - Retrieve all positions for a specific IMEI, with optional date filtering.
 * - Create a new Position entity.
 * - Retrieve a specific Position entity by its ID.
 * - Update an existing Position entity by its ID.
 * - Delete a Position entity by its ID.
 *
 * Private methods are used for auxiliary operations such as filtering positions based on date parameters.
 */
@Slf4j
@RestController
class PositionController extends BaseController {

    private final PositionServiceImpl positionService;
    private final MapStructMapper mapper;

    /**
     * Constructs a PositionController with the specified PositionServiceImpl and MapStructMapper instances.
     *
     * @param positionService the service layer used for handling business logic related to Position entities
     * @param mapper the mapper used for converting between Position entities and DTOs
     */
    PositionController(PositionServiceImpl positionService, @Qualifier("mapStructMapper") MapStructMapper mapper) {
        this.positionService = positionService;
        this.mapper = mapper;
    }

    /**
     * Retrieves the most recent position(s) associated with the specified IMEI.
     *
     * @param imei the unique identifier for the device whose latest positions are to be retrieved
     * @return a ResponseEntity containing an Iterable of PositionDTO objects representing the latest positions
     */
    @GetMapping("/api/v1/imeis/{imei}/positions/last")
    @PreAuthorize("hasRole('GOD') or @imeiSecurity.isOwner(#id)")
    ResponseEntity<Iterable<PositionDTO>> last(@PathVariable String imei) {
        Iterable<PositionDTO> positions = mapper.toPositionDTO(positionService.findLast(imei));
        return ResponseEntity.ok(positions);
    }

    /**
     * Retrieves all positions associated with the specified IMEI, optionally filtered by query parameters.
     *
     * @param imei the unique identifier for the device whose positions are to be retrieved
     * @param params an optional map of query parameters for filtering the positions, such as date ranges
     * @param authorizationHeader an optional authorization header for authentication
     * @return a ResponseEntity containing an Iterable of PositionDTO objects representing the positions
     */
    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/api/v1/imeis/{imei}/positions")
    @PreAuthorize("hasRole('GOD') or @imeiSecurity.isOwner(#id)")
    ResponseEntity<Iterable<PositionDTO>> all(@PathVariable String imei, @RequestParam(required = false) Map<String, String> params, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Iterable<PositionDTO> positions = mapper.toPositionDTO(findPositions(imei, params));
        return ResponseEntity.ok(positions);
    }
    // end::get-aggregate-root[]

    /**
     * Creates a new position based on the provided input and returns the created position's details.
     *
     * @param newPosition the details of the position to be created
     * @return ResponseEntity containing the details of the created position in the response body and an HTTP status of CREATED
     */
    @PostMapping("/api/v1/positions")
    @PreAuthorize("hasRole('GOD')")
    @ResponseBody
    ResponseEntity<PositionDTO> createNewPosition(@RequestBody Position newPosition) {
        Position createdPosition =  positionService.newPosition(newPosition);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toPositionDTO(createdPosition));
    }

    /**
     * Retrieves a single position by its ID.
     *
     * @param id the unique identifier of the position to retrieve
     * @return a ResponseEntity containing the PositionDTO representation of the position
     */
    // Single item
    @GetMapping("/api/v1/positions/{id}")
    @PreAuthorize("hasRole('GOD')")
    @ResponseBody
    ResponseEntity<PositionDTO> one(@PathVariable Long id) {
        Position position = positionService.findById(id);
        return ResponseEntity.ok(mapper.toPositionDTO(position));
    }

    /**
     * Replaces an existing position with the provided position data.
     *
     * @param newPosition the new position data to replace the existing position
     * @param id the identifier of the position to be replaced
     * @return a ResponseEntity containing the updated position data as a PositionDTO object
     */
    @PutMapping("/api/v1/positions/{id}")
    @PreAuthorize("hasRole('GOD')")
    @ResponseBody
    ResponseEntity<PositionDTO> replacePosition(@RequestBody Position newPosition, @PathVariable Long id) {
        Position updatedPosition =  positionService.updatePosition(newPosition,id);
        return ResponseEntity.ok(mapper.toPositionDTO(updatedPosition));
    }

    /**
     * Deletes a position by its unique identifier.
     *
     * @param id the unique identifier of the position to be deleted
     * @return a ResponseEntity with no content indicating the position was successfully deleted
     */
    @DeleteMapping("/api/v1/positions/{id}")
    @PreAuthorize("hasRole('GOD')")
    ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Finds positions for a given device identified by its IMEI, based on the provided parameters.
     * The method retrieves positions within a specified time range if "from" and/or "to" parameters are provided.
     * If no time range is specified, it returns all positions for the given IMEI.
     *
     * @param imei the IMEI of the device for which positions are to be retrieved
     * @param params a map containing query parameters. Valid keys include:
     *               "from" - the start of the time range (inclusive) in a parsable date format
     *               "to" - the end of the time range (exclusive) in a parsable date format
     * @return an iterable collection of positions that match the provided criteria
     */
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
