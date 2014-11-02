package org.kalipo.aop;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.kalipo.config.ErrorCode;
import org.kalipo.web.rest.ExceptionSummary;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by damoeb on 17.09.14.
 */
@Aspect
public class ExceptionHandlerAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around("@within(org.kalipo.aop.KalipoExceptionHandler) || @annotation(org.kalipo.aop.KalipoExceptionHandler)")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {

        try {

            // annotation @ExceptionHandler does not work
            return joinPoint.proceed();

        } catch (ConstraintViolationException e) {
//            return handleConstraintViolationException(e);

            List<String> errors = new LinkedList<>();
            e.getConstraintViolations().forEach(violation -> errors.add(violation.getMessage()));

            throw new KalipoException(ErrorCode.INVALID_PARAMETER, StringUtils.join(errors, ", "));

        } catch (KalipoException e) {
            throw e;
//            return handleKalipoException(e);

        } catch (Throwable e) {
            throw new KalipoException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
        }
    }

    @ExceptionHandler(KalipoException.class)
    private ResponseEntity<ExceptionSummary> handleKalipoException(KalipoException exception) {
        return new ResponseEntity<>(new ExceptionSummary(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<ExceptionSummary> handleConstraintViolationException(ConstraintViolationException exception) {

        List<String> errors = new LinkedList<>();
        exception.getConstraintViolations().forEach(violation -> errors.add(violation.getMessage()));

        return new ResponseEntity<>(new ExceptionSummary(StringUtils.join(errors, ", ")), HttpStatus.BAD_REQUEST);
    }

}
