package com.itsm.controller.bridge.impl;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComLineErrors;
import com.serialpundit.serial.SerialComManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The class implements ComAPI interface using SerialPundit
 * library @see {@link <a href="https://github.com/RishiGupta12/SerialPundit</a>}
 */
public class SerialPundit implements ComAPI {

    private SerialComManager manager;
    private long handle;
    private String port;

    public SerialPundit(String port) throws ControllerCommunicationException {
        this.port = port;
        try {
            manager = new SerialComManager();
            if (manager == null) throw new ControllerCommunicationException("Initialization error");
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    @Override
    public String[] listAvailablePorts() throws ControllerCommunicationException {
        try {
            return manager.listAvailableComPorts();
        } catch (SerialComException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    @Override
    public void openPort() throws ControllerCommunicationException {
        try {

            /* https://github.com/RishiGupta12/SerialPundit/blob/master/javadocs/serial/com/serialpundit/serial/SerialComManager.html

                 Opens a serial port for communication. If an attempt is made to open a port which is already
                 opened, an exception will be thrown.

                 For Linux and Mac OS X, if exclusiveOwnerShip is true, before this method returns, the caller
                 will either be exclusive owner or not. If the caller is successful in becoming exclusive owner than
                 all the attempt to open the same port again will cause native code to return error. Note that a root
                 owned process (root user) will still be able to open the port.

                 For Windows the exclusiveOwnerShip must be true as it does not allow sharing COM ports. An
                 exception is thrown if exclusiveOwnerShip is set to false. For Solaris, exclusiveOwnerShip should be
                 set to false as of now. On Unix-like system this method uses ioctl command TIOCEXCL for exclusive access.
                 and not lock files.

                 This method will clear both input and output buffers of drivers (or operating system).

                 When the serial port is opened DTR and RTS lines will be raised by default by this library. Sometimes,
                 DTR acts as a modem on-hook/off-hook control for other end. Modern modems are highly flexible in their dependency,
                 working and configurations. It is best to consult modem manual. If the application design need DTR/RTS
                 not to be asserted when port is opened custom drivers can be used or hardware can be modified for this
                 purpose. Alternatively, if the application is to be run on Windows operating system only, then modifying
                 INF file or registry key may help in not raising DTR/RTS when port is opened. Typically in Windows DTR/RTS
                 is raised due to enumeration sequence (serenum).

                 In Unix jargon a dial-in TTY device is used for terminals, modems and printers etc. and requires DCD to be
                 high for operation. When used with a modem, the port will wait for carrier before sending out the login prompt to
                 end user. It is for this reason typically DTR of one end is connected to DSR of other end. When the terminal is
                 turned off, any associated jobs are killed, and the user is logged out. Unlike dial-in the dial-out TTY device does
                 not require DCD to be high. Once connection is made DCD may go high. Loss of the DCD signal may cause the jobs
                 to be killed and the user will be automatically logged off.

                 On some hardware when opening a serial port TXD, RXD, RTS or DTR lines may show glitch which may unintentionally
                 trigger other end. This may be due to hardware or a driver bug.

                 This method is thread safe.

             */

            handle = manager.openComPort(this.port, true, true, true);
//            handle = manager.openComPort(this.port, true, true, false);
            manager.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1,
                SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.BCUSTOM, 1200000);
            manager.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            /*
                This method gives more fine tune control to application for tuning performance and behavior of read
                operations to leverage OS specific facility for read operation. The read operations can be optimized
                for receiving for example high volume data speedily or low volume data but received in burst mode.

                If more than one client has opened the same port, then all the clients will be affected by new settings.

                When this method is called application should make sure that previous read or write operation is not in progress.

                Parameters:
                handle - of the opened port
                vmin - c_cc[VMIN] field of termios structure
                vtime - c_cc[VTIME] field of termios structure
                rit - ReadIntervalTimeout field of COMMTIMEOUTS structure
                rttm - ReadTotalTimeoutMultiplier field of COMMTIMEOUTS structure
                rttc - ReadTotalTimeoutConstant field of COMMTIMEOUTS structure
                Returns:
                true on success false otherwise

                // https://msdn.microsoft.com/en-us/library/windows/hardware/hh439614(v=vs.85).aspx
                ReadIntervalTimeout
                    The maximum amount of time, in milliseconds, that is allowed between two consecutive bytes
                    in a read operation. A read operation that exceeds this maximum times out. This maximum
                    does not apply to the time interval that precedes the reading of the first byte.
                    A value of zero indicates that interval time-outs are not used.
                ReadTotalTimeoutMultiplier
                    The maximum amount of time, in milliseconds, that is allowed per byte in a read operation.
                    A read operation that exceeds this maximum times out.
                ReadTotalTimeoutConstant
                    The maximum amount of additional time, in milliseconds, that is allowed per read operation.
                    A read operation that exceeds this maximum times out.

                Read operation that is Nₜₒₜₐₗ bytes in length, the maximum amount of time, Tₘₐₓ,
                that the serial port allows for the operation to complete is calculated as follows:

                    Tₘₐₓ = Nₜₒₜₐₗ * ReadTotalTimeoutMultiplier + ReadTotalTimeoutConstant

             */

            manager.fineTuneReadBehaviour(handle, 0, 5, 50, 5, 100);

        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    @Override
    public int writeBytes(byte[] bytes) throws ControllerCommunicationException {
        try {
            return manager.writeBytes(handle, bytes);
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    /**
     *
     * Writes the bytes from the given direct byte buffer using facilities of
     * the underlying JVM and operating system.
     * https://github.com/RishiGupta12/SerialPundit/blob/master/javadocs/serial/com/serialpundit/serial/SerialComManager.html
     *
     * @param buffer
     * @param offset
     * @param length
     * @return
     * @throws ControllerCommunicationException
     */
    @Override
    public int writeBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException {
        try {
            return manager.writeBytesDirect(handle, buffer, offset, length);
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    @Override
    public byte[] readBytes() throws ControllerCommunicationException {
        try {
            return manager.readBytes(handle, 2048);
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    /**
     *
     * Reads the bytes from the serial port into the given direct byte buffer using facilities of
     * the underlying JVM and operating system.
     * This method does not modify the direct byte buffer attributes position, capacity, limit and mark.
     * The application design is expected to take care of this as and when required in appropriate manner.
     * https://github.com/RishiGupta12/SerialPundit/blob/master/javadocs/serial/com/serialpundit/serial/SerialComManager.html
     *
     * @param buffer
     * @param offset
     * @param length
     * @return
     * @throws ControllerCommunicationException
     */
    @Override
    public int readBytesDirect(ByteBuffer buffer, int offset, int length) throws ControllerCommunicationException {
        try {
            return manager.readBytesDirect(handle, buffer, offset, length);
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

    @Override
    public void closePort() throws ControllerCommunicationException {
        try {
            manager.closeComPort(handle);
        } catch (IOException e) {
            throw new ControllerCommunicationException(e);
        }
    }

}
