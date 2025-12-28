package com.cred.search.models.response.wrapper;

import lombok.Getter;

@Getter
public enum ApiStatusCode {
    SUCCESS("RP200-000", null, 200),
    INVALID_REQUEST("RP400-000", "invalid request", 400),
    ENTITY_NOT_FOUND("RP404-000", "invalid request", 404),
    UNEXPECTED_RUN_TIME_EXCEPTION("RP500-000", "unexpected runtime exception", 500);

    private final String errorCode;
    private final String errorMessage;
    private final Integer statusCode;

    ApiStatusCode(String errorCode, String errorMessage, int statusCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }
}
