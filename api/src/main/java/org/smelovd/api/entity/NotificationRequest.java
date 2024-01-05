package org.smelovd.api.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @Id
    private String id;
    //private byte[] file;
    //TODO filepath to file
    private String message;
    private Timestamp timestamp;
    private NotificationRequestStatus status;
}
