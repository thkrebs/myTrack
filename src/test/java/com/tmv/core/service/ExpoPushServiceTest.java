package com.tmv.core.service;

import com.tmv.core.dto.ExpoPushMessageDTO;
import com.tmv.core.model.User;
import com.tmv.core.model.UserDevice;
import com.tmv.core.persistence.UserDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpoPushServiceTest {

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ExpoPushService expoPushService;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Manually construct service to inject mocked RestClient (since it's created in constructor)
        // But wait, the constructor creates a NEW RestClient.builder().build().
        // I cannot easily mock that without changing the constructor or using PowerMock.
        // Better approach: Refactor Service to accept RestClient.Builder or RestClient in constructor.
        
        // However, for this test, I will use Reflection to set the field after construction if possible,
        // or just rely on the fact that I can't easily mock the internal RestClient without refactoring.
        
        // Let's assume I can refactor the service slightly to be more testable, 
        // OR I use a workaround. 
        
        // Workaround: Create the service, then use ReflectionTestUtils to swap the RestClient.
        expoPushService = new ExpoPushService(userDeviceRepository);
        ReflectionTestUtils.setField(expoPushService, "restClient", restClient);
        ReflectionTestUtils.setField(expoPushService, "expoAccessToken", "test-token");

        testUser = new User();
        testUser.setUsername("testuser");
    }

    @Test
    void sendPushNotification_success() {
        UserDevice device = new UserDevice();
        device.setToken("ExponentPushToken[123]");
        when(userDeviceRepository.findAllByUser(testUser)).thenReturn(List.of(device));

        // Mock the fluent API chain
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(List.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        expoPushService.sendPushNotification(testUser, "Title", "Body", Map.of("key", "val"));

        // Verify the payload
        ArgumentCaptor<List<ExpoPushMessageDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(requestBodySpec).body(captor.capture());
        
        List<ExpoPushMessageDTO> messages = captor.getValue();
        assertEquals(1, messages.size());
        assertEquals("ExponentPushToken[123]", messages.get(0).getTo());
        assertEquals("Title", messages.get(0).getTitle());
        assertEquals("Body", messages.get(0).getBody());
    }

    @Test
    void sendPushNotification_noDevices_doesNotSend() {
        when(userDeviceRepository.findAllByUser(testUser)).thenReturn(Collections.emptyList());

        expoPushService.sendPushNotification(testUser, "Title", "Body", Map.of());

        verify(restClient, never()).post();
    }
}
