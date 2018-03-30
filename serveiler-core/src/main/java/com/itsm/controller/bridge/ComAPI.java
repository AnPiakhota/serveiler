package com.itsm.controller.bridge;

import com.itsm.controller.ControllerCommunicationException;
import com.serialpundit.core.SerialComException;

import java.nio.ByteBuffer;

/**
 * This interface is the interface for communication with
 * chosen implementation for accessing controllers plugged via
 * serial COM port directly or using virtual driver that allows
 * establish virtual COM port and access it via USB port.
 *
 * Alternative implementations: @see {@link <a
 * https://www.quora.com/I-am-trying-to-read-data-from-a-USB-serial-com-port-in-Java-but-could-not-get-it-What-is-a-working-solution-for-this
 * </a>, @link <a https://en.wikibooks.org/wiki/Serial_Programming/Serial_Java </a>}
 *
 */
public interface ComAPI {

    String[] listAvailablePorts() throws ControllerCommunicationException;

    void openPort() throws ControllerCommunicationException;
    int writeBytes(byte[] bytes) throws ControllerCommunicationException;
    int writeBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException;
    byte[] readBytes() throws ControllerCommunicationException;
    int readBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException;
    void closePort() throws ControllerCommunicationException;

}
