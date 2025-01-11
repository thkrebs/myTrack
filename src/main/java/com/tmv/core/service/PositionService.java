package com.tmv.core.service;

import com.tmv.core.model.Position;

import java.time.LocalDateTime;

public interface PositionService {
    Iterable<Position> findAll(String imei);

    Iterable<Position> findLast(String imei);

    Iterable<Position> findBetween(String imei, LocalDateTime from, LocalDateTime to);

    Position newPosition(Position newPosition);

    Position findById(Long id);

    Position updatePosition(Position newPosition, Long id);

    void deletePosition(Long id);
}
