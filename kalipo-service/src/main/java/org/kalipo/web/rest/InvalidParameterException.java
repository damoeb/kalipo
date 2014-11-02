package org.kalipo.web.rest;

import org.kalipo.config.ErrorCode;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by damoeb on 14.09.14.
 */
@ResponseBody
public class InvalidParameterException extends KalipoException {

    public InvalidParameterException(String param) {
        super(ErrorCode.INVALID_PARAMETER, param);
    }
}
