package com.tmv.core.service;

import com.tmv.core.dto.ExpoPushMessageDTO;
import com.tmv.core.model.User;
import com.tmv.core.model.UserDevice;
import com.tmv.core.persistence.UserDeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpoPushService {

    private static final String EXPO_API_URL = "https://exp.host/--/api/v2/push/send";

    private final UserDeviceRepository userDeviceRepository;
    private final RestClient restClient;

    @Value("${expo.access.token:}")
    private String expoAccessToken;

    public ExpoPushService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.restClient = RestClient.builder()
                .baseUrl(EXPO_API_URL)
                .build();
    }

    public void sendPushNotification(User user, String title, String body, Map<String, String> data) {
        List<UserDevice> devices = userDeviceRepository.findAllByUser(user);

        if (devices.isEmpty()) {
            log.debug("No devices found for user: {}", user.getUsername());
            return;
        }

        List<ExpoPushMessageDTO> messages = devices.stream()
                .map(device -> ExpoPushMessageDTO.builder()
                        .to(device.getToken())
                        .title(title)
                        .body(body)
                        .data(data)
                        .sound("default")
                        .build())
                .collect(Collectors.toList());

        try {
            log.info("Sending {} push notifications to user: {}", messages.size(), user.getUsername());
            
            // Expo accepts an array of messages
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + expoAccessToken)
                    .body(messages)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Push notifications sent successfully.");

        } catch (Exception e) {
            log.error("Failed to send push notifications", e);
        }
    }
}
