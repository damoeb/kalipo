package org.kalipo.web.rest;

import org.kalipo.config.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by damoeb on 14.09.14.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KalipoRequestException extends Exception {

    private ErrorCode errorCode;
    private Object resource;

    public KalipoRequestException(ErrorCode errorCode, Object resource) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.resource = resource;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getResource() {
        return resource;
    }
}
