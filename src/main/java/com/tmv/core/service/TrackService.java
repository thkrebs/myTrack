package com.tmv.core.service;

import com.tmv.core.model.Track;
import org.locationtech.jts.geom.LineString;

public interface TrackService {
    LineString currentTrack(String imei);
}
