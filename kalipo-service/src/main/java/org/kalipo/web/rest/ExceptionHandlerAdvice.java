package org.kalipo.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by damoeb on 17.09.14.
 */
@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerAdvice {

    @ExceptionHandler(KalipoRequestException.class)
    private ResponseEntity<ExceptionSummary> handleKalipoException(KalipoRequestException exception) {
        return new ResponseEntity<>(new ExceptionSummary(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<ExceptionSummary> handleConstraintViolationException(ConstraintViolationException exception) {

        List<String> errors = new LinkedList<>();
        exception.getConstraintViolations().forEach(violation -> errors.add(violation.getMessage()));

        return new ResponseEntity<>(new ExceptionSummary(StringUtils.join(errors, ", ")), HttpStatus.BAD_REQUEST);
    }
}
