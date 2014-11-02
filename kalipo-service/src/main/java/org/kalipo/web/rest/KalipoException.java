package org.kalipo.web.rest;

import org.kalipo.config.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by damoeb on 14.09.14.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KalipoException extends Exception {

    private ErrorCode errorCode;
    private Object resource;

    public KalipoException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public KalipoException(ErrorCode errorCode, Object resource) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.resource = resource;
    }

    public KalipoException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getResource() {
        return resource;
    }
}
