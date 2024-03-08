package org.smelovd.api.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;
    private String serviceUserId;
    private String sender;
    private String templateId;
}
