package com.itsm.controller;

/**
 * Created by anpiakhota on 13.12.16.
 */
public class ControllerCommunicationException extends Exception {

    public ControllerCommunicationException() {
    }

    public ControllerCommunicationException(String message) {
        super(message);
    }

    public ControllerCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControllerCommunicationException(Throwable cause) {
        super(cause);
    }

    public ControllerCommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
