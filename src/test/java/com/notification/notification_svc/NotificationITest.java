package com.notification.notification_svc;

import com.notification.notification_svc.model.Notification;
import com.notification.notification_svc.model.NotificationPreference;
import com.notification.notification_svc.model.NotificationStatus;
import com.notification.notification_svc.model.NotificationType;
import com.notification.notification_svc.repository.NotificationPreferenceRepository;
import com.notification.notification_svc.repository.NotificationRepository;
import com.notification.notification_svc.service.NotificationService;
import com.notification.notification_svc.web.dto.NotificationPreferenceResponse;
import com.notification.notification_svc.web.dto.NotificationRequest;
import com.notification.notification_svc.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class NotificationITest {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void createNewNotificationPreferenceHappyPath() {
        UUID userId = UUID.randomUUID();
        UpsertNotificationPreference upsertNotificationPreference = new UpsertNotificationPreference();
        upsertNotificationPreference.setUserId(userId);
        upsertNotificationPreference.setNotificationEnabled(true);
        upsertNotificationPreference.setContactInfo("test@email.com");
        upsertNotificationPreference.setType(NotificationType.EMAIL);

        notificationService.upsertPreference(upsertNotificationPreference);

        List<NotificationPreference> preferences = notificationPreferenceRepository.findAll();
        NotificationPreference notificationPreference = null;

        for (NotificationPreference preference : preferences) {
            if (preference.getUserId().equals(userId)) {
                notificationPreference = preference;
            }
        }

        assertEquals(userId, notificationPreference.getUserId());
        assertEquals(upsertNotificationPreference.getContactInfo(), notificationPreference.getContactInfo());
    }
    @Test
    void sendNotificationHappyPath() {
        UUID userId = UUID.randomUUID();
        NotificationRequest notificationRequest = NotificationRequest
                .builder()
                .userId(userId)
                .body("Test body")
                .subject("Test subject")
                .build();


        UpsertNotificationPreference upsertNotificationPreference = new UpsertNotificationPreference();
        upsertNotificationPreference.setUserId(userId);
        upsertNotificationPreference.setNotificationEnabled(true);
        upsertNotificationPreference.setContactInfo("test@email.com");
        upsertNotificationPreference.setType(NotificationType.EMAIL);

        notificationService.upsertPreference(upsertNotificationPreference);

        notificationService.sendNotification(notificationRequest);

        List<Notification> notifications = notificationRepository.findAll();

        for (Notification notification : notifications) {
            if (notification.getUserId().equals(userId)) {
                assertEquals(userId, notification.getUserId());
            }
        }
    }
    @Test
    void retryFailedNotificationHappyPath() {
        UUID userId = UUID.randomUUID();
        NotificationRequest notificationRequest = NotificationRequest
                .builder()
                .userId(userId)
                .body("Test body")
                .subject("Test subject")
                .build();


        UpsertNotificationPreference upsertNotificationPreference = new UpsertNotificationPreference();
        upsertNotificationPreference.setUserId(userId);
        upsertNotificationPreference.setNotificationEnabled(true);
        upsertNotificationPreference.setContactInfo("damqnskiqq@gmail.com");
        upsertNotificationPreference.setType(NotificationType.EMAIL);

        notificationService.upsertPreference(upsertNotificationPreference);

        notificationService.sendNotification(notificationRequest);

        notificationService.clearNotifications(userId);

        List<Notification> notifications = notificationRepository.findAllByUserIdAndStatus(userId, NotificationStatus.FAILED);

        for (Notification notification : notifications) {
            assertEquals(true, notification.isDeleted());
        }





    }
}
