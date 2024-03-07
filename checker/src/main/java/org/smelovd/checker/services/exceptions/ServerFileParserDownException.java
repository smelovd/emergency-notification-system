package org.smelovd.checker.services.exceptions;

import lombok.Getter;
import org.smelovd.checker.entities.NotificationTemplate;

@Getter
public class ServerFileParserDownException extends Throwable {
    private final NotificationTemplate template;

    public ServerFileParserDownException(NotificationTemplate template) {
        super();
        this.template = template;
    }

}
