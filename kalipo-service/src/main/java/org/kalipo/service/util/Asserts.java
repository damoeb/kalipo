package org.kalipo.service.util;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Thread;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoException;

/**
 * Created by damoeb on 26.09.14.
 */
public final class Asserts {
    public static void isNotNull(Object o, String paramName) throws KalipoException {
        if (o == null) {
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

    public static void isNotReadOnly(Thread thread) throws KalipoException {
        if (thread.getReadOnly()) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "thread is readonly");
        }
    }

    public static void nullOrEqual(String found, String expected, String fieldName) throws KalipoException {
        if (!StringUtils.equals(found, expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s cannot be modified", fieldName));
        }
    }

    public static void nullOrEqual(Integer found, Integer expected, String fieldName) throws KalipoException {
        if (found != null && !found.equals(expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s cannot be modified", fieldName));
        }
    }

    public static void isCurrentLogin(String authorId) throws KalipoException {
        if (!StringUtils.equals(SecurityUtils.getCurrentLogin(), authorId)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("Only %s can modify", authorId));
        }
    }

    public static void nullOrEqual(Object found, Object expected, String fieldName) throws KalipoException {
        if (found != null && !found.equals(expected)) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, String.format("%s cannot be modified", fieldName));
        }
    }
}
