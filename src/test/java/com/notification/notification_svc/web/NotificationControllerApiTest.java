package com.notification.notification_svc.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification_svc.model.Notification;
import com.notification.notification_svc.model.NotificationPreference;
import com.notification.notification_svc.model.NotificationStatus;
import com.notification.notification_svc.model.NotificationType;
import com.notification.notification_svc.service.NotificationService;
import com.notification.notification_svc.web.dto.NotificationPreferenceResponse;
import com.notification.notification_svc.web.dto.NotificationResponse;
import com.notification.notification_svc.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationPreference notificationPreference;

    private final ObjectMapper mapper = new ObjectMapper();

    private UUID userId;
    private List<NotificationResponse> notificationResponses;
    private NotificationPreferenceResponse preferenceResponse;

    @BeforeEach
    void setUp() {
        notificationPreference = new NotificationPreference();
        notificationPreference.setEnabled(true);
        notificationPreference.setType(NotificationType.EMAIL);
        notificationPreference.setId(UUID.randomUUID());
        notificationPreference.setUpdatedOn(LocalDateTime.now());
        notificationPreference.setContactInfo("Test@gmail.com");

        userId = UUID.randomUUID();
        notificationResponses = List.of(NotificationResponse.builder()
                        .subject("Test subject")
                        .createdAt(LocalDateTime.now())
                        .status(NotificationStatus.SUCCEEDED)
                        .type(NotificationType.EMAIL)
                .build());
        preferenceResponse = NotificationPreferenceResponse.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .enabled(true)
                .contactInfo("Test@gmail.com")
                .build();

    }

    @Test
    void getRequestNotificationPreference_happyPath() throws Exception {

        when(notificationService.getPreferenceByUserId(any())).thenReturn(notificationPreference);
        MockHttpServletRequestBuilder request = get("/api/v1/notifications/preferences").param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("type").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("enabled").isNotEmpty());
    }

    @Test
    void postWithBodyToCreatePreference_returns201AndCorrectDtoStructure() throws Exception {

        UpsertNotificationPreference requestDto = UpsertNotificationPreference.builder()
                .userId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .contactInfo("test@example.com")
                .notificationEnabled(true)
                .build();

        when(notificationService.upsertPreference(any())).thenReturn(notificationPreference);

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("type").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("enabled").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("contactInfo").isNotEmpty());
    }
    @Test
    void getNotificationHistory_ShouldReturnListOfNotifications() throws Exception {
        when(notificationService.getNotificationHistory(userId))
                .thenReturn(List.of(
                        Notification.builder()
                                .subject("Test1")
                                .type(NotificationType.EMAIL)
                                .status(NotificationStatus.FAILED)
                                .updatedOn(LocalDateTime.now())
                                .body("Test2")
                                .contactInfo("Test@gmail.com")
                                .createdOn(LocalDateTime.now())
                                .updatedOn(LocalDateTime.now())
                                .build(),
                        Notification.builder()
                                .subject("Test2")
                                .type(NotificationType.EMAIL)
                                .status(NotificationStatus.FAILED)
                                .updatedOn(LocalDateTime.now())
                                .body("Test Body")
                                .contactInfo("Test12@gmail.com")
                                .createdOn(LocalDateTime.now())
                                .updatedOn(LocalDateTime.now())
                                .build()));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").isNotEmpty());
    }

    @Test
    void changeNotificationPreference_ShouldReturnUpdatedPreference() throws Exception {
        when(notificationService.changeNotificationPreference(userId, true))
                .thenReturn(NotificationPreference.builder()
                        .userId(userId)
                        .type(NotificationType.EMAIL)
                        .enabled(true)
                        .updatedOn(LocalDateTime.now())
                        .createdOn(LocalDateTime.now())
                        .build());

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .param("userId", userId.toString())
                        .param("enabled", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("type").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("userId").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("enabled").isNotEmpty());
    }
}
