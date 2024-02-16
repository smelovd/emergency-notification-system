package org.smelovd.worker_test.services.exceptions;

import org.springframework.http.HttpStatusCode;

public class ServiceException extends RuntimeException {
    public ServiceException(String message, HttpStatusCode httpStatusCode) {
        super(message + " " + httpStatusCode);
    }
}
