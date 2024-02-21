package org.smelovd.worker.services.exceptions;

import org.springframework.http.HttpStatusCode;

public class ServiceException extends RuntimeException {
    public ServiceException(String message, HttpStatusCode httpStatusCode) {
        super(message + " " + httpStatusCode);
    }
}
