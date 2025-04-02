package com.notification.notification_svc.web.dto;

import com.notification.notification_svc.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpsertNotificationPreference {
    @NotNull
    private UUID userId;

    private boolean notificationEnabled;

    @NotNull
    private NotificationType type;

    private String contactInfo;
}
