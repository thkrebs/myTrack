package com.tmv.core.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public  class LatLng2Country {

    public static String getCountry(double latitude, double longitude)  {
        
        // OpenStreetMap Nominatim API
        String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                + latitude + "&lon=" + longitude + "&accept-language=de";
        try {
            URI uri = URI.create(urlStr);
            URL url = uri.toURL();

            JsonNode jsonNode = getJsonNode(url);
            return jsonNode.get("address").get("country").asText();
        } catch (Exception e) {
            log.error("Exception trying to retrieve the country", e); 
            return "-";
        } 
    }

    private static JsonNode getJsonNode(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Java/GeocodingExample");

        // process response
        InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder jsonBuffer = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            jsonBuffer.append((char) c);
        }
        reader.close();

        // extract country
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonBuffer.toString());
        return jsonNode;
    }
}
