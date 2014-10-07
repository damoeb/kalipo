package org.kalipo.service.util;

import org.kalipo.config.ErrorCode;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;

/**
 * Created by damoeb on 26.09.14.
 */
public final class Asserts {
    public static void isNotNull(Object o, String paramName) throws KalipoRequestException {
        if (o == null) {
            throw new KalipoRequestException(ErrorCode.INVALID_PARAMETER, paramName);
        }

    }

    public static void isNull(Object o, String paramName) throws KalipoRequestException {
        if (o != null) {
            throw new KalipoRequestException(ErrorCode.INVALID_PARAMETER, paramName);
        }
    }

    public static void hasPrivilege(String privilege) throws KalipoRequestException {
        if (!SecurityUtils.hasPrivilege(privilege)) {
            throw new KalipoRequestException(ErrorCode.PERMISSION_DENIED, "uriHook");
        }
    }
}
