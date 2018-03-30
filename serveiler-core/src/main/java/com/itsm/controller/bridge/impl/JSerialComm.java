package com.itsm.controller.bridge.impl;

import com.fazecast.jSerialComm.SerialPort;
import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.serialpundit.core.SerialComException;

import java.nio.ByteBuffer;

/**
 * For future possible implementation
 *
 * The class implements ComAPI interface using jSerialComm
 * library @see {@link <a href="https://github.com/Fazecast/jSerialComm</a>}
 */
public class JSerialComm implements ComAPI {

    /* TODO

        SerialPort commPort0 = SerialPort.getCommPort("/dev/ttyUSB0");
        SerialPort[] commPorts = SerialPort.getCommPorts();

        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        try {
            while (true) {
                while (comPort.bytesAvailable() == 0)
                    Thread.sleep(20);

                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");
            }
        } catch (Exception e) { e.printStackTrace(); }
        comPort.closePort();

    */

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
