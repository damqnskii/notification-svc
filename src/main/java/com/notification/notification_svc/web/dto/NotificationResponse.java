package com.notification.notification_svc.web.dto;

import com.notification.notification_svc.model.NotificationStatus;
import com.notification.notification_svc.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private String subject;

    private LocalDateTime createdAt;

    private NotificationStatus status;

    private NotificationType type;
}
