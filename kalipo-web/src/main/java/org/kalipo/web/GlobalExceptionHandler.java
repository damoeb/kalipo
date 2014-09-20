package org.kalipo.web;

import org.kalipo.web.rest.KalipoRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by damoeb on 17.09.14.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    public static class ExceptionSummary {
        private int errorCode;
        private Object resource;
        private String errorMessage;

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

    @ExceptionHandler(KalipoRequestException.class)
    private ResponseEntity<ExceptionSummary> handleKalipoException(KalipoRequestException exception) {
        return new ResponseEntity<>(new ExceptionSummary(exception), HttpStatus.BAD_REQUEST);
    }
}
