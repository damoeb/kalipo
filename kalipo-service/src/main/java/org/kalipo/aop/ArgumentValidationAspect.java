package org.kalipo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kalipo.config.ErrorCode;
import org.kalipo.web.rest.InvalidParameterException;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by damoeb on 17.09.14.
 */
@Aspect
public class ArgumentValidationAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around("@within(org.kalipo.aop.EnableArgumentValidation) || @annotation(org.kalipo.aop.EnableArgumentValidation)")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {

        try {

            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            checkArguments(joinPoint, signature);

            return joinPoint.proceed();

            // todo exception handling should be done in GlobalExceptionHandler
        } catch (ConstraintViolationException e) {
            List<String> errors = new LinkedList<>();
            e.getConstraintViolations().forEach(violation -> errors.add(violation.getMessage()));

            throw new KalipoRequestException(ErrorCode.CONSTRAINT_VIOLATED, e);

        } catch (KalipoRequestException e) {
            throw e;

        } catch (Throwable e) {
            log.error("Unexpected exception.", e);
            throw new KalipoRequestException(ErrorCode.UNKNOWN_ERROR, e);
        }
    }

    private void checkArguments(ProceedingJoinPoint joinPoint, MethodSignature signature) throws KalipoRequestException {

        String[] names = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < names.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                throw new InvalidParameterException(names[i]);
            }
        }
    }
}
