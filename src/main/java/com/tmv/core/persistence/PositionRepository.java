package com.tmv.core.persistence;

import com.tmv.core.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByImei(String imei);

    Position findById(long id);

    List<Position> findTopByImeiOrderByDateTimeDesc(String imei);

    List<Position> findByImeiOrderByDateTimeAsc(String imei);

    List<Position> findByImeiAndDateTimeBetween(String imei, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Position> findByImeiAndDateTimeGreaterThanEqual(String imei, LocalDateTime startDate);

    List<Position>  findByImeiAndDateTimeLessThanEqual(String imei, LocalDateTime endDate);
    List<Position> findByImeiAndDateTimeBetweenOrderByDateTimeAsc(String imei, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Position> findByImeiInOrderByDateTimeAsc(Collection<String> imeiStrings);
    List<Position> findByImeiInAndDateTimeBetweenOrderByDateTimeAsc(Collection<String> imeiStrings, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(value = "SELECT * FROM position WHERE imei = ?1 AND dateTime >= ?2 AND dateTime <= ?3 AND ST_DistanceSphere(point, ST_SetSRID(ST_MakePoint(?4, ?5), 4326)) >= ?6", nativeQuery = true)
    List<Position> findByImeiInAndDateTimeConcealedOrderByDateTimeAsc(Collection<String> imeiStrings, LocalDateTime startDateTime, LocalDateTime endDateTime, double lng, double lat, long distanceInMeters);
}