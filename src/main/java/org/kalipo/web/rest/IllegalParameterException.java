package org.kalipo.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by damoeb on 14.09.14.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseBody
public class IllegalParameterException extends KalipoRequestException {

}
