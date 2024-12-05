package com.tmv.position;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends CrudRepository<Position, Long> {

    List<Position> findByImei(String imei);

    Position findById(long id);
    
    
}