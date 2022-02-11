package com.meitalk.auth.exception;

public enum  ExceptionCode {

    UNKNOWN_ERROR(-100, "unknown error"),
    WRONG_TYPE_TOKEN(-99, "wrong type token"),
    EXPIRED_TOKEN(-88, "expired token"),
    UNSUPPORTED_TOKEN(-77, "unsupported token"),
    WRONG_TOKEN(-66, "wrong token"),
    ACCESS_DENIED(-44, "access denied");

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ExceptionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
