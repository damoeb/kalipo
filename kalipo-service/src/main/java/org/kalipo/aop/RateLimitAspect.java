package org.kalipo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.kalipo.throttle.RateLimitService;
import org.kalipo.web.rest.KalipoException;

import javax.inject.Inject;

/**
 * Aspect for methods that are annotated with @org.kalipo.aop.RateLimit
 * <p>
 * Created by damoeb on 17.09.14.
 */
@Aspect
public class RateLimitAspect {

    @Inject
    private RateLimitService rateLimitService;

    @Pointcut("@annotation(org.kalipo.aop.RateLimit)")
    public void throttledMethod() {
    }

    @Before("throttledMethod()")
    public void enter(JoinPoint joinPoint) throws KalipoException {

        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        rateLimitService.enter(signature.toLongString());
    }

    @Before("throttledMethod()")
    public void exit(JoinPoint joinPoint) {

        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        rateLimitService.exit(signature.toLongString());
    }

}
