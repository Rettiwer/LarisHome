package com.rettiwer.pl.laris.data.remote.api;

import android.accessibilityservice.GestureDescription;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.SocketTimeoutException;

import androidx.annotation.StringDef;

public class RequestExceptionEvent {
    public static final String TIMEOUT = "TIMEOUT";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    public static final String UNKNOWN_API_ERROR = "UNKNOWN_ERROR";
    public static final String UNKNOWN_ERROR = "UNKNOWN_API_ERROR";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";

    @RequestExceptionType String requestExceptionType;

    private String unknownError;

    public RequestExceptionEvent(@RequestExceptionType String requestExceptionType) {
        this.requestExceptionType = requestExceptionType;
    }

    public RequestExceptionEvent(@RequestExceptionType String requestExceptionType, String unknownError) {
        this.requestExceptionType = requestExceptionType;
        this.unknownError = unknownError;
    }

    @RequestExceptionType
    public String getRequestExceptionType() {
        return this.requestExceptionType;
    }

    public String getUnknownError() {
        return this.unknownError;
    }

    @StringDef({TIMEOUT, NETWORK_ERROR, UNKNOWN_API_ERROR, UNKNOWN_ERROR, INVALID_TOKEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestExceptionType {
    }
}