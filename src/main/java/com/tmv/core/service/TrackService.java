package com.tmv.core.service;

import org.locationtech.jts.geom.LineString;

public interface TrackService {
    LineString currentTrack(String imei);
}
