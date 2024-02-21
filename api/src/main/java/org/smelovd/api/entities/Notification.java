package org.smelovd.api.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;
    private String fileId;
    private String serviceUserId;
    private String notificationService;
    private String notificationId;
    private Date timestamp;
    private NotificationStatus status;
}
