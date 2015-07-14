package org.kalipo.service.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Thread;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoException;

/**
 * Assertion support to minimize redundancy
 * Created by damoeb on 26.09.14.
 */
public final class Asserts {

    public static void isNotNull(Object o, String paramName) throws KalipoException {
        if (o == null) {
            throw new KalipoException(ErrorCode.INVALID_PARAMETER, paramName);
        }
    }

    public static void isNotEmpty(String o, String paramName) throws KalipoException {
        if (StringUtils.isEmpty(o)) {
            throw new KalipoException(ErrorCode.INVALID_PARAMETER, paramName);
        }
    }

    public static void isNull(Object o, String paramName) throws KalipoException {
        if (o != null) {
            throw new KalipoException(ErrorCode.INVALID_PARAMETER, paramName);
        }
    }

    public static void hasPrivilege(String privilege) throws KalipoException {
        if (!SecurityUtils.hasPrivilege(privilege)) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED, "uriHook");
        }
    }

    public static void isNotLocked(Thread thread) throws KalipoException {
        if (Thread.Status.LOCKED == thread.getStatus()) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "thread is locked");
        }
    }

    public static void nullOrEqual(String found, String expected, String fieldName) throws KalipoException {
        if (found != null && !StringUtils.equals(found, expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s is null", fieldName));
        }
    }

    public static void nullOrEqual(Integer found, Integer expected, String fieldName) throws KalipoException {
        if (found != null && !found.equals(expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s is null", fieldName));
        }
    }

    public static void isCurrentLogin(String authorId) throws KalipoException {
        if (!StringUtils.equals(SecurityUtils.getCurrentLogin(), authorId)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("Only %s may continue", authorId));
        }
    }

    public static void nullOrEqual(Object found, Object expected, String fieldName) throws KalipoException {
        if (found != null && !found.equals(expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s is null", fieldName));
        }
    }

    public static void nullOrEqual(DateTime found, DateTime expected, String fieldName) throws KalipoException {
        if (found != null && !found.isEqual(expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s is null", fieldName));
        }
    }

    public static void isTrue(boolean trueOrFalse, String errMessage) throws KalipoException {
        if (!trueOrFalse) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, errMessage);
        }
    }
}
