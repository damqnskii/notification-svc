package com.notification.notification_svc.web.mapper;

import com.notification.notification_svc.model.Notification;
import com.notification.notification_svc.model.NotificationPreference;
import com.notification.notification_svc.web.dto.NotificationPreferenceResponse;
import com.notification.notification_svc.web.dto.NotificationResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference entity) {

        return NotificationPreferenceResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .contactInfo(entity.getContactInfo())
                .enabled(entity.isEnabled())
                .userId(entity.getUserId())
                .build();
    }

    public static NotificationResponse fromNotification(Notification entity) {

        return NotificationResponse.builder()
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedOn())
                .type(entity.getType())
                .body(entity.getBody())
                .build();
    }
}
