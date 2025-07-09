package com.tmv.core.controller;


import com.tmv.core.dto.ImeiDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.model.Imei;
import com.tmv.core.service.ImeiServiceImpl;
import com.tmv.core.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * ImeiController handles CRUD operations for the Imei entity.
 * It exposes RESTful endpoints for creating, retrieving, updating, and deleting Imei resources.
 * Provides pagination support for retrieving a list of Imeis.
 * The controller uses a MapStruct mapper for data transformation between DTO and entity.
 * It also applies exception handling from the BaseController for consistent error handling.
 */
@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class ImeiController extends BaseController {

    private final ImeiServiceImpl imeiService;
    private final MapStructMapper mapper;

    ImeiController(@Qualifier("mapStructMapper") MapStructMapper mapstructMapper, ImeiServiceImpl imeiService) {
        this.mapper = mapstructMapper;
        this.imeiService = imeiService;
    }

    /**
     * Creates a new Imei resource based on the provided ImeiDTO and persists it in the database.
     *
     * @param newImeiDTO the data transfer object containing the details of the Imei to be created
     * @return a ResponseEntity containing the created Imei entity and a status of 201 (Created)
     */
    @PostMapping("/api/v1/imeis")
    @ResponseBody
    ResponseEntity<Imei> createImei(@Valid @RequestBody ImeiDTO newImeiDTO) {
        Imei createdImei = imeiService.createNewImei(mapper.toImeiEntity(newImeiDTO));
        return ResponseEntity.status(201).body(createdImei);
    }

    /**
     * Retrieves a single Imei resource by its unique ID.
     * The method fetches the Imei entity from the database,
     * converts it to a DTO, and returns it wrapped in a ResponseEntity.
     * If the requested Imei is not found, a ResourceNotFoundException is thrown.
     *
     * @param id the unique identifier of the Imei resource to retrieve
     * @return a ResponseEntity containing the ImeiDTO representation of the requested Imei
     */
    // Single item
    @GetMapping("/api/v1/imeis/{id}")
    @PreAuthorize("hasRole('GOD') or @imeiSecurity.isOwner(#id)")
    @ResponseBody
    ResponseEntity<ImeiDTO> one(@PathVariable Long id) {
        Imei imei = imeiService.getImeiById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imei not found with id: " + id));
        return ResponseEntity.ok(mapper.toImeiDTO(imei));
    }

    /**
     * Retrieves a list of all Imei resources for the authenticated user.
     * Converts the Imei entities to their corresponding ImeiDTO representations.
     *
     * @return a ResponseEntity containing a list of ImeiDTO objects
     */
    @GetMapping(path = "/api/v1/imeis")
    ResponseEntity<List<ImeiDTO>> all() {
        List<ImeiDTO> imeiList = mapper.toImeiDTOList(imeiService.allForUser());
        // Return ResponseEntity with the content
        return ResponseEntity.ok(imeiList);
    }


    /**
     * Updates the journey associated with a specific IMEI.
     *
     * @param newImei The updated information for the IMEI.
     * @param id The unique identifier of the IMEI to be updated.
     * @return A ResponseEntity containing the updated IMEI information as a DTO.
     */
    @PutMapping("/api/v1/imeis/{id}")
    @PreAuthorize("hasRole('GOD') or @imeiSecurity.isOwner(#id)")
    @ResponseBody
    ResponseEntity<ImeiDTO> updateJourney(@RequestBody ImeiDTO newImei, @PathVariable Long id) {
        Imei updatedImei = imeiService.updateImei(id,mapper.toImeiEntity(newImei));
        return ResponseEntity.ok(mapper.toImeiDTO(updatedImei));
    }

    /**
     * Deletes a journey associated with the specified ID.
     *
     * @param id the unique identifier of the journey to be deleted
     * @return a ResponseEntity with no content indicating the deletion was successful
     */
    @DeleteMapping("/api/v1/imeis/{id}")
    @PreAuthorize("hasRole('GOD') or @imeiSecurity.isOwner(#id)")
    @ResponseBody
    ResponseEntity<Void> deleteJourney(@PathVariable Long id) {
        imeiService.deleteImei(id);
        return ResponseEntity.noContent().build();
    }
}
