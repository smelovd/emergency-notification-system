package org.smelovd.worker.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@Builder(toBuilder = true)
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;
    private String serviceUserId;
    private String notificationService;
    private String requestId;
    private Date createdAt;
    private Date lastUpdatedAt;
    private NotificationStatus status;
}
