package org.kalipo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.kalipo.throttle.MethodThrottleService;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Aspect for methods that are annotated with @org.kalipo.aop.Throttled
 * <p>
 * Created by damoeb on 17.09.14.
 */
@Aspect
public class MethodThrottleAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private MethodThrottleService methodThrottleService;

    @Pointcut("@annotation(org.kalipo.aop.Throttled)")
    public void throttledMethod() {
    }

    @Before("throttledMethod()")
    public void enter(JoinPoint joinPoint) throws KalipoRequestException {

        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Throttled annotation = signature.getMethod().getAnnotation(Throttled.class);
        methodThrottleService.initAndEnter(signature.toLongString(), annotation.limit());
    }

    @Before("throttledMethod()")
    public void exit(JoinPoint joinPoint) {

        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        methodThrottleService.exit(signature.toLongString());
    }

}
