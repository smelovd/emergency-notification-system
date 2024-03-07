package org.smelovd.checker.entities;

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
public class NotificationRequestHistory {

    @Id
    private String id;
    private String requestId;
    private Date createdAt;
    private Date completedAt;
    private String status;
}
