package com.tmv.core.dto;

import com.tmv.core.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring"
)
public interface MapStructMapper {
    @Mapping(target = "ownerId", source = "owner.id") // Map owner's ID to ownerId
    ImeiDTO toImeiDTO(Imei imei);

    List<ImeiDTO> toImeiDTOList(List<Imei> imeiList);

    ImeiSlimDTO toImeiSlimDTO(Imei imei);

    @Mapping(target = "journeys", ignore = true)
    @Mapping(target = "owner", ignore = true) // Ignore owner when mapping from the ImeiDTO to Entity
    Imei toImeiEntity(ImeiDTO imeiDTO);

    //@Mapping(target = "parkSpots", source = "overnightParkings")
    //@Mapping(target = "parkSpots", ignore = true) // will be mapped manually
    @Mapping(target = "parkSpots",
            expression = "java(overnightParkingsToDto(journey.getOvernightParkings()))")
    @Mapping(target = "ownerId", source = "owner.id") // Map owner's ID to ownerId
    JourneyDTO toJourneyDTO(com.tmv.core.model.Journey journey);

    @Mapping(target = "owner", ignore = true) // Ignore owner when mapping from a DTO to an Entity
    @Mapping(target = "overnightParkings", ignore = true)
    @Mapping(target = "id", ignore = true)
    Journey toJourneyEntity(CreateJourneyDTO journeyDTO);

    @Mapping(target = "lat", expression = "java(parkSpot.getPoint() != null ? (float) parkSpot.getPoint().getY() : 0f)")
    @Mapping(target = "lng", expression = "java(parkSpot.getPoint() != null ? (float) parkSpot.getPoint().getX() : 0f)")
    ParkSpotDTO toParkSpotDTO(ParkSpot parkSpot);


    @Mapping(target = "lat", expression = "java((float) position.getPoint().getY())")
    @Mapping(target = "lng", expression = "java((float) position.getPoint().getX())")
    PositionDTO toPositionDTO(com.tmv.core.model.Position position);

    Iterable<PositionDTO> toPositionDTO(Iterable<com.tmv.core.model.Position> positions);

    List<ImeiSlimDTO> toImeiSlimDTO(List<Imei> imeiList);

    List<ParkSpotDTO> toParkSpotDTO(List<ParkSpot> parkSpotList);


    @Mapping(target = "id", expression = "java(new com.tmv.core.model.OvernightParkingId(overnightParkingDTO.getJourneyId(), overnightParkingDTO.getParkSpotId(), overnightParkingDTO.getOvernightDate()))")
    @Mapping(target = "journey", ignore = true)
    @Mapping(target = "parkSpot", ignore = true)
    @Mapping(target = "overnightDate", ignore = true)
    OvernightParking toOvernightParkingEntity(OvernightParkingDTO overnightParkingDTO);

    @Mapping(target = "parkSpotId", expression = "java(overnightParking.getId().getParkSpotId())")
    @Mapping(target = "journeyId", expression = "java(overnightParking.getId().getJourneyId())")
    @Mapping(target = "overnightDate")
    OvernightParkingDTO toOvernightParkingDTO(OvernightParking overnightParking);


    @Mapping(target = "parkspotId", source = "parkSpot.id")
    @Mapping(target = "name", expression = "java(overnightParking.getParkSpot().getName())")
    @Mapping(target = "description", expression = "java(overnightParking.getParkSpot().getDescription())")
    @Mapping(target = "lat", expression = "java((float) overnightParking.getParkSpot().getPoint().getY())")
    @Mapping(target = "lng", expression = "java((float) overnightParking.getParkSpot().getPoint().getX())")
    @Mapping(target = "overnightDate", source = "overnightDate")
    @Mapping(target = "wpPostId", source = "parkSpot.wpPostId")
    OvernightParkingFullDTO toOvernightParkingFullDTO(OvernightParking overnightParking);


    @Mapping(target = "userId", source = "user.id") // Map user's ID to userId in the DTO
    ApiTokenDTO toApiTokenDTO(ApiToken apiToken);

    List<ApiTokenDTO> toApiTokenDTO(List<ApiToken> apiTokens);


    default List<ParkSpotDTO> extractParkSpotsFromOvernightParkings(List<OvernightParking> overnightParkings) {
        if (overnightParkings == null) return null;

        return overnightParkings.stream()
                .map(OvernightParking::getParkSpot) // Extrahiere ParkSpot-Entität
                .map(this::toParkSpotDTO) // Konvertiere zu ParkSpotDTO
                .toList(); // Java 16+ oder .collect(Collectors.toList()) in älteren Versionen
    }

    default List<OvernightParkingFullDTO> toOvernightParkingFullDTOList(List<OvernightParking> overnightParkings) {
        if (overnightParkings == null || overnightParkings.isEmpty()) {
            return null;
        }

        return overnightParkings.stream()
                .map(this::toOvernightParkingFullDTO) // Map each OvernightParking to OvernightParkingFullDTO
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void mapOvernightParkingsToParkSpots(Journey journey, @MappingTarget JourneyDTO journeyDTO) {
        journeyDTO.setParkSpots(
                toOvernightParkingFullDTOList(journey.getOvernightParkings()
                        .stream()
                        .toList()));
    }

    // New helper method to map the list of OvernightParkings
    default List<OvernightParkingFullDTO> overnightParkingsToDto(Set<OvernightParking> overnightParkings) {
        if (overnightParkings == null) return null;

        return overnightParkings.stream()
                .map(this::toOvernightParkingFullDTO) // Map each OvernightParking
                .collect(Collectors.toList());
    }

    default Page<ImeiDTO> pagedImeiToPagedImeiDto(Page<Imei> pageEntity) {
        final Page<ImeiDTO> pageDto = pageEntity.map(this::toImeiDTO);
        return pageDto;
    }

    default Set<Imei> map(List<Long> imeiIds) {
        if (imeiIds == null) {
            return null;
        }
        return imeiIds.stream()
                .map(id -> {
                    Imei imei = new Imei();
                    imei.setId(id);
                    return imei;
                })
                .collect(Collectors.toSet());
    }
}
