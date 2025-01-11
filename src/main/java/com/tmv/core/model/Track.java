package com.tmv.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Track {
    private LineString lineString;
    private Map<String, Object> properties = new HashMap<String, Object>();

    public void addProperty(String key, Object value)
    {
        this.properties.put(key,value);
    }
}

