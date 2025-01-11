package com.tmv.core.service;

import com.tmv.core.model.Position;
import com.tmv.core.model.Track;
import com.tmv.core.persistence.PositionRepository;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrackServiceImpl implements TrackService {

    private final PositionRepository positionRepository;
    private final GeometryFactory geomFactory;

    TrackServiceImpl(PositionRepository positionRepository) {
        super();
        this.geomFactory = new GeometryFactory();
        this.positionRepository = positionRepository;
    }

    public LineString currentTrack(String imei) {
        List<Position> positions =  positionRepository.findByImeiOrderByDateTimeAsc(imei);
        LineString ls = geomFactory.createLineString( positions.stream()
                .map(position -> new Coordinate(position.getPoint().getX(), position.getPoint().getY()))
                .toArray(Coordinate[]::new));
        //Track t = new Track();
        //t.setLineString(ls);
        //t.addProperty("imei", imei);
        return ls; //t;
    }
}
