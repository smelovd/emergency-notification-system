package org.smelovd.checker.services.exceptions;

import lombok.Getter;
import org.smelovd.checker.entities.NotificationRequest;

@Getter
public class ServerFileParserDownException extends Throwable {
    private final NotificationRequest request;

    public ServerFileParserDownException(NotificationRequest request) {
        super();
        this.request = request;
    }

}
