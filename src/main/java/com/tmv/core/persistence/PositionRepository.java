package com.tmv.core.persistence;

import com.tmv.core.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    List<Position> findByImeiInAndDateTimeGreaterThanEqualOrderByDateTimeAsc(Collection<String> imeiStrings, LocalDateTime startDate);
    List<Position> findByImeiInAndDateTimeLessThanEqualOrderByDateTimeAsc(Collection<String> imeiStrings, LocalDateTime endDate);
}