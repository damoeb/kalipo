package org.kalipo.config;

/**
 * Created by damoeb on 17.09.14.
 */
public enum ErrorCode {

    UNKNOWN_ERROR(1, "An unknown error occurred"),
    APP_REQUEST_LIMIT_REACHED(1, "Application request limit reached"),
    USER_REQUEST_LIMIT_REACHED(1, "User request limit reached"),
    INVALID_PARAMETER(1, "Invalid parameter"),
    PERMISSION_DENIED(1, "Permission denied"),;
//    BANNED(1, "you are banned"),
//    REPUTATION(2, "reputation is too low"),
//    FREQUENCY(3, "use is time limited (temporary lock)"),
//    RESOURCE_NOT_FOUND(4, "does not exist"),
//    INVALID_STATUS(5, "status is invalid"),
//    ALREADY_EXISTS(6, "already exists"),
//    INVALID_DATA(7, "invalid data"),
//    BLOCKED(8, "Request is blocked");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
