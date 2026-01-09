package com.tmv.core.service;

import com.tmv.core.model.ParkSpot;

import java.util.List;
import java.util.Map;

public interface ParkSpotService {
    List<ParkSpot> findParkSpotsWithinDistance(double longitude, double latitude, double distanceInMeters);
    Map<String, Object> debugParkSpot(Long id);
}
