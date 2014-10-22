package org.kalipo.web.rest;

/**
 * Created by damoeb on 22.10.14.
 */
public class ExceptionSummary {
    private int errorCode;
    private Object resource;
    private String errorMessage;

    public ExceptionSummary(String message) {
        this.errorMessage = message;
    }

    public ExceptionSummary(KalipoRequestException exception) {
        this.errorMessage = exception.getMessage();
        this.errorCode = exception.getErrorCode().getCode();
        this.resource = exception.getResource();
    }

    public Object getResource() {
        return resource;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}