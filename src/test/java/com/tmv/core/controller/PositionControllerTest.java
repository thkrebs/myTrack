package com.tmv.core.controller;


import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.model.Position;
import com.tmv.core.service.ImeiValidationService;
import com.tmv.core.service.PositionService;
import com.tmv.core.service.PositionServiceImpl;
import com.tmv.core.util.MultiFormatDateParser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.tmv")
@PropertySource("classpath:application.properties")
@Import(CoreConfiguration.class)
public class PositionControllerTest {

    @MockBean
    ImeiValidationService imeiValidationService;

    @MockBean(PositionServiceImpl.class)
    PositionService positionService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private PositionController controller;

    final String imei = "123";
    final Date dt = new GregorianCalendar(2023, Calendar.OCTOBER, 31).getTime();

    final Position position = new Position(8f,50f, (short) 1, (short) 2, (byte) 3, (short) 4,imei,dt);


    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }


    @Test
    public void testInvalidImei() throws Exception {

        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(false);

        mockMvc.perform(get("/imei/123/positions/last"))
                .andExpect(status().is4xxClientError());
    }


    @Test
    void shouldReturnLastPositionNotEmpty() throws Exception {
        Mockito.when(positionService.findLast(imei)).thenReturn(List.of(position));
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions/last")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$.*.point.coordinates[0]").value(8)) // Validate specific JSON property (example field 'latitude')
                .andExpect(jsonPath("$.*.point.coordinates[1]").value(50))
                .andExpect(jsonPath("$.*.altitude").value(1))
                .andExpect(jsonPath("$.*.angle").value(2))
                .andExpect(jsonPath("$.*.satellites").value(3))
                .andExpect(jsonPath("$.*.speed").value(4))
                .andExpect(jsonPath("$.*.imei").value(imei));
                //.andExpect(jsonPath("$.*.dateTime").value(dt.toString()));
         // Validate ;
    }

    @Test
    void shouldReturnLastPositionEmpty() throws Exception {

        Mockito.when(positionService.findLast(imei)).thenReturn(List.of());
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions/last")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$").isEmpty());
         // Validate ;
    }

    @Test
    void shouldReturnAllPositions() throws Exception {
        Mockito.when(positionService.findAll(imei)).thenReturn(List.of(position));
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$").isNotEmpty());
         // Validate ;
    }

    @Test
    void shouldReturnAllPositionsUntilToDate() throws Exception {
        String dateTo = "04-08-2015 10:11";
        Mockito.when(positionService.findBetween(imei,null, MultiFormatDateParser.parseDate(dateTo))).thenReturn(List.of(position));
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions?to=" + dateTo)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$").isNotEmpty());
         // Validate ;
    }

    @Test
    void shouldReturnAllPositionsFromDate() throws Exception {
        String dateFrom = "04-08-2015 10:11";
        Mockito.when(positionService.findBetween(imei, MultiFormatDateParser.parseDate(dateFrom), null)).thenReturn(List.of(position));
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions?from=" + dateFrom)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$").isNotEmpty());
         // Validate ;
    }

    @Test
    void shouldReturnAllPositionsFromToDate() throws Exception {
        String dateFrom = "04-08-2015 10:11";
        String dateTo = "05-08-2015 10:11";
        Mockito.when(positionService.findBetween(imei, MultiFormatDateParser.parseDate(dateFrom), MultiFormatDateParser.parseDate(dateTo))).thenReturn(List.of(position));
        Mockito.when(imeiValidationService.isActive(imei)).thenReturn(true);
        this.mockMvc.perform(get("/api/v1/imei/" + imei + "/positions?from=" + dateFrom + "&to=" + dateTo)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Check that the response is JSON
                .andExpect(jsonPath("$").isNotEmpty());
        // Validate ;
    }
}
