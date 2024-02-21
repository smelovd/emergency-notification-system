package org.smelovd.worker.exceptions;

import org.springframework.http.HttpStatusCode;

public class ServiceSenderException extends RuntimeException {
    public ServiceSenderException(String message, HttpStatusCode httpStatusCode) {
        super(message + " " + httpStatusCode);
    }
}
