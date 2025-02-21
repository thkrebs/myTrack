package com.tmv.core.controller;


import com.tmv.core.dto.ImeiDTO;
import com.tmv.core.dto.MapStructMapper;
import com.tmv.core.model.Imei;
import com.tmv.core.service.ImeiServiceImpl;
import com.tmv.core.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class ImeiController extends BaseController {

    private final ImeiServiceImpl imeiService;
    private final MapStructMapper mapper;

    ImeiController(MapStructMapper mapstructMapper, ImeiServiceImpl imeiService) {
        this.mapper = mapstructMapper;
        this.imeiService = imeiService;
    }

    @PostMapping("/api/v1/imeis")
    @ResponseBody
    ResponseEntity<Imei> createImei(@Valid @RequestBody ImeiDTO newImeiDTO) {
        Imei createdImei = imeiService.createNewImei(mapper.toImeiEntity(newImeiDTO));
        return ResponseEntity.status(201).body(createdImei);
    }

    // Single item
    @GetMapping("/api/v1/imeis/{id}")
    @ResponseBody
    ResponseEntity<ImeiDTO> one(@PathVariable Long id) {
        Imei imei = imeiService.getImeiById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imei not found with id: " + id));
        return ResponseEntity.ok(mapper.toImeiDTO(imei));
    }

    @GetMapping(path = "/api/v1/imeis", params = { "page", "size" })
    ResponseEntity<List<ImeiDTO>> allPaginated(@RequestParam("page") int page,
                                    @RequestParam("size") int size, UriComponentsBuilder uriBuilder,
                                    HttpServletResponse response) {
        Page<ImeiDTO> resultPage = mapper.pagedImeiToPagedImeiDto(imeiService.allPaginated(page, size));
        if (page > resultPage.getTotalPages()) {
            throw new ResourceNotFoundException("No more Imeis");
        }
        // Add pagination metadata to headers
        response.addHeader("X-Total-Pages", String.valueOf(resultPage.getTotalPages()));
        response.addHeader("X-Total-Elements", String.valueOf(resultPage.getTotalElements()));

        // Return ResponseEntity with the paginated content
        return ResponseEntity.ok(resultPage.getContent());
    }

    @PutMapping("/api/v1/imeis/{id}")
    @ResponseBody
    ResponseEntity<ImeiDTO> updateJourney(@RequestBody ImeiDTO newImei, @PathVariable Long id) {
        Imei updatedImei = imeiService.updateImei(id,mapper.toImeiEntity(newImei));
        return ResponseEntity.ok(mapper.toImeiDTO(updatedImei));
    }

    @DeleteMapping("/api/v1/imeis/{id}")
    @ResponseBody
    ResponseEntity<Void> deleteJourney(@PathVariable Long id) {
        imeiService.deleteImei(id);
        return ResponseEntity.noContent().build();
    }
}
