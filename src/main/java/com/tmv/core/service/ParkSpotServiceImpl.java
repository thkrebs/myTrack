package com.tmv.core.service;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Journey;
import com.tmv.core.model.OvernightParking;
import com.tmv.core.model.ParkSpot;
import com.tmv.core.persistence.JourneyRepository;
import com.tmv.core.persistence.ParkSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParkSpotServiceImpl implements ParkSpotService {

    private final ParkSpotRepository parkSpotRepository;

    @Autowired
    public ParkSpotServiceImpl(ParkSpotRepository parkSpotRepository) {
        this.parkSpotRepository = parkSpotRepository;
    }

    @Override
    public List<ParkSpot> findParkSpotsWithinDistance(double longitude, double latitude, double distanceInMeters) {
        return parkSpotRepository.findWithinDistance(longitude, latitude, distanceInMeters);
    }

    @Override
    public Map<String, Object> debugParkSpot(Long id) {
        ParkSpot parkSpot = parkSpotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkSpot not found with id: " + id));
        String wkt = parkSpotRepository.getPointAsText(id);

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("id", parkSpot.getId());
        debugInfo.put("java_x_lng", parkSpot.getPoint().getX());
        debugInfo.put("java_y_lat", parkSpot.getPoint().getY());
        debugInfo.put("db_wkt", wkt);
        return debugInfo;
    }
}
