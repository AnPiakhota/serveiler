package com.itsm.controller;

/**
 * Created by anpiakhota on 13.12.16.
 */
public class FacadeApiMishandleException extends Exception {

    public FacadeApiMishandleException() {
    }

    public FacadeApiMishandleException(String message) {
        super(message);
    }

    public FacadeApiMishandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public FacadeApiMishandleException(Throwable cause) {
        super(cause);
    }

    public FacadeApiMishandleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
