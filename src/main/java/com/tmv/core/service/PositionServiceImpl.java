package com.tmv.core.service;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Position;
import com.tmv.core.persistence.PositionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    PositionServiceImpl(PositionRepository positionRepository) {
        super();
        this.positionRepository = positionRepository;
    }

    @Override
    public Iterable<Position> findAll(String imei) {
        return positionRepository.findByImei(imei);
    }

    @Override
    public Position newPosition(Position newPosition) {
        return positionRepository.save(newPosition);
    }

    @Override
    public Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + id));
    }

    @Override
    public Position updatePosition(Position newPosition, Long id) {
        return positionRepository.findById(id)
                .map(positionRepository::save)
                .orElseGet(() -> {
                    return positionRepository.save(newPosition);
                });
    }

    @Override
    public void deletePosition(Long id) {
        positionRepository.deleteById(id);
    }

    public Iterable<Position> findLast(String imei) {
        return positionRepository.findTopByImeiOrderByDateTimeDesc(imei);
    }

    public Iterable<Position> findBetween(String imei, LocalDateTime from, LocalDateTime to) {
        Iterable<Position> result = null;
        if ((from != null) && (to != null)) {
            result = positionRepository.findByImeiAndDateTimeBetween(imei,from,to);
        }
        else if (to == null) {
            result = positionRepository.findByImeiAndDateTimeGreaterThanEqual(imei,from);
        }
        else {
            result = positionRepository.findByImeiAndDateTimeLessThanEqual(imei,to);
        }
        return result;
    }
}

