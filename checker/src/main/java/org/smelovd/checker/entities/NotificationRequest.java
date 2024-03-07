package org.smelovd.checker.entities;

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
public class NotificationRequest {

    @Id
    private String id;
    private String message;
    private Date createdAt;
    private Long notificationCount;
    private boolean isParsed;

}
