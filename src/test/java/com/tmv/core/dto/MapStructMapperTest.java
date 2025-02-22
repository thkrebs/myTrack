package com.tmv.core.dto;

import com.tmv.core.config.CoreConfiguration;
import com.tmv.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MapStructMapperTest {

    private MapStructMapper mapper;

    @BeforeEach
    void setUp() {
        // Initialisiere MapStruct Mapper
        mapper = Mappers.getMapper(MapStructMapper.class);
    }

    // TEST 1: Map von DTO → Entität
    @Test
    void shouldMapOvernightParkingDTOToEntity() {
        OvernightParkingDTO dto = new OvernightParkingDTO();
        dto.setJourneyId(1L);
        dto.setOvernightDate(LocalDate.of(2023, 10, 1));
        dto.setParkSpotId(5L);

        // Act: Map DTO zu Entity
        OvernightParking entity = mapper.toOvernightParkingEntity(dto);

        // Assert: Validierung des Mappings
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getId().getJourneyId()).isEqualTo(1L);
        assertThat(entity.getOvernightDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(entity.getId().getParkSpotId()).isEqualTo(5L);
    }

    @Test
    void shouldMapOvernightParkingEntityToDTO() {
        OvernightParkingId id = new OvernightParkingId(1L, 5L, LocalDate.of(2023, 10, 1));
        ParkSpot parkSpot = new ParkSpot();
        parkSpot.setId(5L);
        Journey journey = new Journey();
        journey.setId(1L);

        OvernightParking entity = new OvernightParking();
        entity.setId(new OvernightParkingId(1L, 5L, LocalDate.of(2023, 10, 1)));
        entity.setOvernightDate(LocalDate.of(2023, 10, 1));

        // Act: Map Entity zu DTO
        OvernightParkingDTO dto = mapper.toOvernightParkingDTO(entity);

        // Assert: Validierung des Mappings
        assertThat(dto).isNotNull();
        assertThat(dto.getJourneyId()).isEqualTo(1L);
        assertThat(dto.getOvernightDate()).isEqualTo(LocalDate.of(2023, 10, 1));
        assertThat(dto.getParkSpotId()).isEqualTo(5L);
    }

    // TEST 3: Handling von `null` inputs für DTO → Entität
    @Test
    void shouldReturnNullWhenMappingNullDTOToEntity() {
        // Act: Map Null DTO zu Entität
        OvernightParking entity = mapper.toOvernightParkingEntity(null);

        // Assert: Resultat sollte Null sein
        assertThat(entity).isNull();
    }

    // TEST 4: Handling von `null` inputs für Entität → DTO
    @Test
    void shouldReturnNullWhenMappingNullEntityToDTO() {
        // Act: Map Null Entity zu DTO
        OvernightParkingDTO dto = mapper.toOvernightParkingDTO(null);

        // Assert: Resultat sollte Null sein
        assertThat(dto).isNull();
    }

    // TEST 5: Vollständiger Map mit leeren Werten für DTO → Entität
    @Test
    void shouldHandleEmptyDTOToEntity() {
        // Arrange: Erstelle leeres DTO
        OvernightParkingDTO dto = new OvernightParkingDTO();

        // Act: Map DTO zu Entity
        OvernightParking entity = mapper.toOvernightParkingEntity(dto);

        // Assert: Validierung des Mappings
        assertThat(entity).isNotNull();
        assertThat(entity.getId().getJourneyId()).isNull(); // Keine ID vorhanden
        assertThat(entity.getId().getParkSpotId()).isNull(); // Keine ID vorhanden

        assertThat(entity.getOvernightDate()).isNull(); // Kein ParkSpot vorhanden
    }

    // TEST 6: Vollständiger Map mit leeren Werten für Entität → DTO
    @Test
    void shouldHandleEmptyEntityToDTO() {
        // Arrange: Erstelle leere Entität
        OvernightParking entity = new OvernightParking();

        // Act: Map Entity zu DTO
        OvernightParkingDTO dto = mapper.toOvernightParkingDTO(entity);

        // Assert: Validierung des Mappings
        assertThat(dto).isNotNull();
        assertThat(dto.getJourneyId()).isNull(); // Kein Journey ID vorhanden
        assertThat(dto.getOvernightDate()).isNull(); // Kein Overnight-Date
        assertThat(dto.getParkSpotId()).isNull(); // Kein ParkSpot ID
    }

    // TEST 7: Fehlerhafte Daten verarbeiten DTO → Entität
    @Test
    void shouldHandleInvalidDataDTOToEntity() {
        // Arrange: Erstelle ein DTO mit falschen Werten
        OvernightParkingDTO dto = new OvernightParkingDTO();
        dto.setJourneyId(null); // Falscher Wert
        dto.setOvernightDate(null); // Falscher Wert
        dto.setParkSpotId(-1L); // Ungültige ID

        // Act: Map DTO zu Entity
        OvernightParking entity = mapper.toOvernightParkingEntity(dto);

        // Assert: Validierung des Mappings
        assertThat(entity).isNotNull();
        assertThat(entity.getId().getJourneyId()).isNull(); // Keine ID wegen falscher Daten
        assertThat(entity.getId().getParkSpotId()).isEqualTo(-1L); // Keine ID wegen falscher Daten
        assertThat(entity.getOvernightDate()).isNull();
    }

    // TEST 8: Fehlerhafte Daten verarbeiten Entität → DTO
    @Test
    void shouldHandleInvalidDataEntityToDTO() {
        // Arrange: Erstelle eine Entität mit falschen Werten
        LocalDate date = LocalDate.now();

        OvernightParking entity = new OvernightParking();
        entity.setId(new OvernightParkingId());
        entity.setOvernightDate(null);

        // Act: Map Entity zu DTO
        OvernightParkingDTO dto = mapper.toOvernightParkingDTO(entity);

        // Assert: Validierung des Mappings
        assertThat(dto).isNotNull();
        assertThat(dto.getJourneyId()).isNull(); // Keine Journey ID
        assertThat(dto.getOvernightDate()).isNull(); // Kein Datum
        assertThat(dto.getParkSpotId()).isNull(); // ParkSpot ID ist null
    }


    @Test
    void testToImeiDTO() {
        Imei imei = new Imei();
        imei.setId(1L);
        imei.setImei("123456789");

        ImeiDTO imeiDTO = mapper.toImeiDTO(imei);

        assertNotNull(imeiDTO);
        assertEquals(1L, imeiDTO.getId());
        assertEquals("123456789", imeiDTO.getImei());
    }

    @Test
    void testToImeiSlimDTO() {
        Imei imei = new Imei();
        imei.setId(1L);

        ImeiSlimDTO imeiSlimDTO = mapper.toImeiSlimDTO(imei);

        assertNotNull(imeiSlimDTO);
        assertEquals(1L, imeiSlimDTO.getId());
    }

    @Test
    void testToJourneyEntity() {
        CreateJourneyDTO createJourneyDTO = new CreateJourneyDTO();
        createJourneyDTO.setName("Test Journey");

        Journey journey = mapper.toJourneyEntity(createJourneyDTO);

        assertNotNull(journey);
        assertEquals("Test Journey", journey.getName());
    }

    @Test
    void testToParkSpotDTO() {
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), CoreConfiguration.SRID);

        ParkSpot parkSpot = new ParkSpot();
        Point p = geometryFactory.createPoint( new Coordinate(2.345, 48.856) );
        parkSpot.setPoint(p);

        ParkSpotDTO parkSpotDTO = mapper.toParkSpotDTO(parkSpot);

        assertNotNull(parkSpotDTO);
        assertEquals(2.345f, parkSpotDTO.getLng());
        assertEquals(48.856f, parkSpotDTO.getLat());
    }

    @Test
    void testToPositionDTO() {
        LocalDateTime dateTime = LocalDateTime.now();
        Position position = new Position(1.234f, 5.678f, (short) 1, (short) 2, (byte) 3, (short) 4, "12345", dateTime);
        PositionDTO positionDTO = mapper.toPositionDTO(position);

        assertNotNull(positionDTO);
        assertEquals(1.234f, positionDTO.getLat());
        assertEquals(5.678f, positionDTO.getLng());
        assertEquals(1, positionDTO.getAltitude());
        assertEquals(2, positionDTO.getAngle());
        assertEquals(3, positionDTO.getSatellites());
        assertEquals(4, positionDTO.getSpeed());
        assertEquals("12345", positionDTO.getImei());
        assertEquals(dateTime, positionDTO.getDateTime());
    }

    @Test
    void testExtractParkSpotsFromOvernightParkings() {
        OvernightParking overnightParking1 = new OvernightParking();
        ParkSpot parkSpot1 = new ParkSpot();
        parkSpot1.setId(1L);

        OvernightParking overnightParking2 = new OvernightParking();
        ParkSpot parkSpot2 = new ParkSpot();
        parkSpot2.setId(2L);

        overnightParking1.setParkSpot(parkSpot1);
        overnightParking2.setParkSpot(parkSpot2);

        List<ParkSpotDTO> parkSpotDTOs = mapper.extractParkSpotsFromOvernightParkings(List.of(overnightParking1, overnightParking2));

        assertNotNull(parkSpotDTOs);
        assertEquals(2, parkSpotDTOs.size());
        assertEquals(1L, parkSpotDTOs.get(0).getId());
        assertEquals(2L, parkSpotDTOs.get(1).getId());
    }

    @Test
    void testPagedImeiToPagedImeiDto() {
        Date from = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from); // Datum setzen
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Einen Tag hinzufügen

        Date to = calendar.getTime();

        List<Imei> imeiList = List.of(
                new Imei("123", true, from, to, "01512"),
                new Imei("456", false, from, to, "0171")
        );
        Page<Imei> imeiPage = new PageImpl<>(imeiList);

        Page<ImeiDTO> imeiDTOPage = mapper.pagedImeiToPagedImeiDto(imeiPage);

        assertNotNull(imeiDTOPage);
        assertEquals(2, imeiDTOPage.getTotalElements());
        assertEquals("123", imeiDTOPage.getContent().get(0).getImei());
        assertTrue(imeiDTOPage.getContent().get(0).isActive());
        assertEquals(from, imeiDTOPage.getContent().get(0).getValidFrom());
        assertEquals(to, imeiDTOPage.getContent().get(0).getValidTo());
        assertEquals("01512", imeiDTOPage.getContent().get(0).getPhoneNumber());
        assertEquals("456", imeiDTOPage.getContent().get(1).getImei());
        assertFalse(imeiDTOPage.getContent().get(1).isActive());
        assertEquals(from, imeiDTOPage.getContent().get(1).getValidFrom());
        assertEquals(to, imeiDTOPage.getContent().get(1).getValidTo());
        assertEquals("0171", imeiDTOPage.getContent().get(1).getPhoneNumber());
    }

    @Test
    void testMapToSetOfImei() {
        List<Long> imeiIds = List.of(100L, 200L);

        Set<Imei> imeis = mapper.map(imeiIds);

        assertNotNull(imeis);
        assertEquals(2, imeis.size());
        assertTrue(imeis.stream().anyMatch(imei -> imei.getId() == 100L));
        assertTrue(imeis.stream().anyMatch(imei -> imei.getId() == 200L));
    }
}