package com.notification.notification_svc.service;

import com.notification.notification_svc.model.Notification;
import com.notification.notification_svc.model.NotificationPreference;
import com.notification.notification_svc.model.NotificationStatus;
import com.notification.notification_svc.model.NotificationType;
import com.notification.notification_svc.repository.NotificationPreferenceRepository;
import com.notification.notification_svc.repository.NotificationRepository;
import com.notification.notification_svc.web.dto.NotificationRequest;
import com.notification.notification_svc.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationUTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private NotificationPreference preference;
    private UpsertNotificationPreference upsertPreference;
    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        preference = NotificationPreference.builder()
                .userId(userId)
                .contactInfo("user@example.com")
                .enabled(true)
                .type(NotificationType.EMAIL)
                .updatedOn(LocalDateTime.now())
                .build();
        upsertPreference = UpsertNotificationPreference
                .builder()
                .userId(userId)
                .contactInfo("user@example.com")
                .notificationEnabled(true)
                .type(NotificationType.EMAIL)
                .build();

    }

    @Test
    void testUpsertPreference_CreateNew() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenReturn(preference);

        NotificationPreference result = notificationService.upsertPreference(upsertPreference);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("user@example.com", result.getContactInfo());
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void testUpsertPreference_UpdateExisting() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);

        NotificationPreference result = notificationService.upsertPreference(upsertPreference);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(preferenceRepository).save(preference);
    }

    @Test
    void testGetPreferenceByUserId_Success() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.of(preference));

        NotificationPreference result = notificationService.getPreferenceByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void testGetPreferenceByUserId_NotFound() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NullPointerException.class, () -> {
            notificationService.getPreferenceByUserId(userId);
        });

        assertEquals("Notification preference for user id %s was not found.".formatted(userId), exception.getMessage());
    }


    @Test
    void testChangeNotificationPreference_WhenUserExists_ShouldUpdateAndSave() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);

        NotificationPreference updatedPreference = notificationService.changeNotificationPreference(userId, false);

        assertNotNull(updatedPreference);
        assertFalse(updatedPreference.isEnabled());
        verify(preferenceRepository).save(preference);
    }
    @Test
    void testChangeNotificationPreference_WhenUserNotFound_ShouldThrowException() {
        when(preferenceRepository.findNotificationPreferenceByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                notificationService.changeNotificationPreference(userId, false)
        );

        verify(preferenceRepository, never()).save(any());
    }
}
