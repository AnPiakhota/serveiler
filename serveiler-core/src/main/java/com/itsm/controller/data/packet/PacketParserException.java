package com.itsm.controller.data.packet;

/**
 * Created by anpiakhota on 13.12.16.
 */
public class PacketParserException extends Exception {

    public PacketParserException() {
    }

    public PacketParserException(String message) {
        super(message);
    }

    public PacketParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketParserException(Throwable cause) {
        super(cause);
    }

    public PacketParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
