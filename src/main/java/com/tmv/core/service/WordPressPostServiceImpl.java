package com.tmv.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmv.core.util.LatLng2Country;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.io.IOException;


@Slf4j
@Service
public class WordPressPostServiceImpl implements WordPressPostService{
    @Value("${wordpress.apiUrl}")
    private String apiUrl;

    @Value("${wordpress.username}")
    private String username;

    @Value("${WP_PWD}")
    private String applicationPassword;

    @Value("${wordpress.category}")
    private int category;

    @Value("${wordpress.status}")
    private String status;

    // returns the id of WordPress post
    public Integer createPost(String title, String content, double lat, double lng)  {
        Result result = null;

        // Generate the Basic Authentication header
        String auth = username + ":" + applicationPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

        // Create the JSON payload
        String jsonBody = "{"
                + "\"title\": \"" + title + "\","
                + "\"content\": \"" + content + "\","
                + "\"status\": \"" + status + "\","  // e.g., publish, draft, etc.
                + "\"categories\": [" +  category + "],"
                + "\"acf\": {"
                + "    \"lat\": \"" + lat + "\","
                + "    \"lng\": \"" + lng+ "\","
                + "    \"land\": \"" + LatLng2Country.getCountry(lat,lng) + "\","
                + "    \"letzterbesuch\":  \"" + currentMonthAndYear() + "\""
                + "}"
                + "}";

        // Call the WordPress API
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Prepare the POST request
            HttpPost httpPost = new HttpPost(apiUrl + "/wp-json/wp/v2/posts");
            httpPost.setHeader("Authorization", authHeader);
            httpPost.setHeader("Content-Type", "application/json");

            // Set the request body
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // Execute the request
            //HttpResponse response = client.execute(httpPost);
            result = client.execute(httpPost, response -> {
                log.info("New Wordpress post created. Response status: {}", httpPost + "->" + new StatusLine(response));
                    // Process response message and convert it into a value object
                return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
            });
        } catch (IOException e) {
            log.error("Error while making the API request: ", e);
        }
        return getId(result);
    }

    private String currentMonthAndYear() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return currentDate.format(formatter);
    }

    private Integer getId(Result result) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(result.content);
            return jsonNode.get("id").asInt(-1);
        } catch (JsonProcessingException e) {
            return -1;
        }

    }

    static class Result {

        final int status;
        final String content;

        Result(final int status, final String content) {
            this.status = status;
            this.content = content;
        }

    }
}