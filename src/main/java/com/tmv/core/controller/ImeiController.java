package com.tmv.core.controller;


import com.tmv.core.model.Imei;
import com.tmv.core.model.Journey;
import com.tmv.core.service.ImeiService;
import com.tmv.core.service.ImeiServiceImpl;
import com.tmv.core.service.JourneyServiceImpl;
import com.tmv.core.service.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
//TODO Move Cross Origin to global config https://spring.io/guides/gs/rest-service-cors
public class ImeiController extends BaseController {

    private final ImeiServiceImpl imeiService;

    ImeiController(ImeiServiceImpl imeiService) {
        this.imeiService = imeiService;
    }

    @PostMapping("/api/v1/imeis")
    @ResponseBody
    Imei createImei(@RequestBody Imei newImei) {
        return imeiService.createNewImei(newImei);
    }

    // Single item
    @GetMapping("/api/v1/imei/{id}")
    @ResponseBody
    Imei one(@PathVariable Long id) {
        return imeiService.getImeiById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imei not found with id: " + id));
    }

    @GetMapping(path = "/api/v1/imeis", params = { "page", "size" })
    public List<Imei> allPaginated(@RequestParam("page") int page,
                                    @RequestParam("size") int size, UriComponentsBuilder uriBuilder,
                                    HttpServletResponse response) {
        Page<Imei> resultPage = imeiService.allPaginated(page, size);
        if (page > resultPage.getTotalPages()) {
            throw new ResourceNotFoundException("No more Imeis");
        }
        return resultPage.getContent();
    }

    @PutMapping("/api/v1/imei/{id}")
    @ResponseBody
    Imei updateJourney(@RequestBody Imei newImei, @PathVariable Long id) {
        return imeiService.updateImei(id,newImei);
    }

    @DeleteMapping("/api/v1/imei/{id}")
    @ResponseBody
    void deleteJourney(@PathVariable Long id) {
        imeiService.deleteImei(id);
    }
}
