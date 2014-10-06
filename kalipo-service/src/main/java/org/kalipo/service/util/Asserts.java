package org.kalipo.service.util;

import org.kalipo.config.ErrorCode;
import org.kalipo.web.rest.KalipoRequestException;

/**
 * Created by damoeb on 26.09.14.
 */
public final class Asserts {
    public static void notNull(Object o, String paramName) throws KalipoRequestException {
        if (o == null) {
            throw new KalipoRequestException(ErrorCode.INVALID_PARAMETER, paramName);
        }

    }
}
