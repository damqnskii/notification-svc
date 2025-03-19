package com.notification.notification_svc.service;

import com.notification.notification_svc.model.Notification;
import com.notification.notification_svc.model.NotificationPreference;
import com.notification.notification_svc.model.NotificationStatus;
import com.notification.notification_svc.model.NotificationType;
import com.notification.notification_svc.repository.NotificationPreferenceRepository;
import com.notification.notification_svc.repository.NotificationRepository;
import com.notification.notification_svc.web.dto.NotificationRequest;
import com.notification.notification_svc.web.dto.UpsertNotificationPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, NotificationPreferenceRepository preferenceRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.mailSender = mailSender;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference preference) {
        Optional<NotificationPreference> userNotificationPreferenceOptional = preferenceRepository.findUserById(preference.getUserId());

        if (userNotificationPreferenceOptional.isPresent()) {
            NotificationPreference userNotificationPreference = userNotificationPreferenceOptional.get();
            userNotificationPreference.setUserId(preference.getUserId());
            userNotificationPreference.setContactInfo(preference.getContactInfo());
            userNotificationPreference.setType(preference.getType());
            userNotificationPreference.setUpdatedOn(LocalDateTime.now());
            userNotificationPreference.setEnabled(preference.isNotificationEnabled());
            return preferenceRepository.save(userNotificationPreference);
        }

        NotificationPreference userNotificationPreference = NotificationPreference.builder()
                .userId(preference.getUserId())
                .contactInfo(preference.getContactInfo())
                .createdOn(LocalDateTime.now())
                .enabled(preference.isNotificationEnabled())
                .type(preference.getType())
                .updatedOn(LocalDateTime.now())
                .build();

        return preferenceRepository.save(userNotificationPreference);
    }
    public NotificationPreference getPreferenceByUserId(UUID userId) {
        return preferenceRepository.findUserById(userId).orElseThrow(() -> new NullPointerException("Notification preference for user id %s was not found.".formatted(userId)));
    }
    public Notification sendNotification(NotificationRequest notificationRequest) {

        UUID userId = notificationRequest.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getBody());

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {

        return notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);
    }

    public NotificationPreference changeNotificationPreference(UUID userId, boolean enabled) {

        NotificationPreference notificationPreference = getPreferenceByUserId(userId);
        notificationPreference.setEnabled(enabled);
        return preferenceRepository.save(notificationPreference);
    }

    public void clearNotifications(UUID userId) {

        List<Notification> notifications = getNotificationHistory(userId);

        notifications.forEach(notification -> {
            notification.setDeleted(true);
            notificationRepository.save(notification);
        });
    }

    public void retryFailedNotifications(UUID userId) {

        NotificationPreference userPreference = getPreferenceByUserId(userId);
        if (!userPreference.isEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        List<Notification> failedNotifications = notificationRepository.findAllByUserIdAndStatus(userId, NotificationStatus.FAILED);
        failedNotifications = failedNotifications.stream().filter(notification ->  !notification.isDeleted()).toList();

        for (Notification notification : failedNotifications) {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userPreference.getContactInfo());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());

            try {
                mailSender.send(message);
                notification.setStatus(NotificationStatus.SUCCEEDED);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
            }

            notificationRepository.save(notification);
        }
    }

}
