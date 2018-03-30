package com.itsm.controller.bridge.impl;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.serialpundit.core.SerialComException;

import java.nio.ByteBuffer;

/**
 * For future possible implementation
 *
 * The class implements ComAPI interface using JSSC (Java Simple Serial Connector)
 * library @see {@link <a href="https://code.google.com/archive/p/java-simple-serial-connector/</a>}
 */
public class JSSC implements ComAPI {

    @Override
    public String[] listAvailablePorts() throws ControllerCommunicationException {
        return new String[0];
    }

    @Override
    public void openPort() throws ControllerCommunicationException {

    }

    @Override
    public int writeBytes(byte[] bytes) throws ControllerCommunicationException {
        return 0;
    }

    @Override
    public int writeBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException {
        return 0;
    }

    @Override
    public byte[] readBytes() throws ControllerCommunicationException {
        return new byte[0];
    }

    @Override
    public int readBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException {
        return 0;
    }

    @Override
    public void closePort() throws ControllerCommunicationException {

    }

}
