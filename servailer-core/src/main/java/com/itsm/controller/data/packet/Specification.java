package com.itsm.controller.data.packet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

/**
 * Created by anpiakhota on 20.12.16.
 */
public interface Specification {

    byte DIRECTION_TO = 0x003E; // >
    byte DIRECTION_FROM = 0x003C; // <

    byte PING_STATUS_ON = 0x000F;
    byte PING_STATUS_OFF = 0x0000;

    byte CONFIRMATION_STATUS_SUCCESS = 0x000F;
    byte CONFIRMATION_STATUS_FAILURE = 0x0000;

    byte BUFFER_STATUS_ON = 0x000F;
    byte BUFFER_STATUS_OFF = 0x0000;

    /**
     * Source: http://stackoverflow.com/questions/3128148/best-way-to-reverse-bytes-in-an-int-in-java
     * @param b
     * @return
     */
    static byte reverseByte(int b) {
        return (byte) (Integer.reverse(b) >>> (Integer.SIZE - Byte.SIZE));
    }

    /**
     * Source: http://stackoverflow.com/questions/3842828/converting-little-endian-to-big-endian
     * @param b
     * @return
     */
    static byte reverseByte(byte b) {
        int converted = 0x00;
        converted ^= (b & 0b1000_0000) >> 7;
        converted ^= (b & 0b0100_0000) >> 5;
        converted ^= (b & 0b0010_0000) >> 3;
        converted ^= (b & 0b0001_0000) >> 1;
        converted ^= (b & 0b0000_1000) << 1;
        converted ^= (b & 0b0000_0100) << 3;
        converted ^= (b & 0b0000_0010) << 5;
        converted ^= (b & 0b0000_0001) << 7;
        return (byte) (converted & 0xFF);
    }

    /**
     * Depth 0 displays simple char representation.
     * Depth 1 displays hex code of the character
     * Depth 2 displays binary code of the character
     * @param buffer
     * @param depth
     * @return
     */
    static String toMonitorString(ByteBuffer buffer, int depth) {

        if (buffer == null) return null;

        buffer.position(0);

        StringBuilder sb = new StringBuilder();

        try {

            /* Direction */

            byte b0 = buffer.get();
            sb.append(Character.toString((char) b0));

            if (depth == 1) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + String.format("%04x", b0 & 0xff));
                sb.append(System.lineSeparator());
            }

            if (depth == 2) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + String.format("%04x", b0 & 0xff));

                sb.append(System.lineSeparator()).append("\t").append("Bin: ");
                for (int i = 7; i >= 0; --i) sb.append(b0 >>> i & 1);
                sb.append(System.lineSeparator());
            }

            /* Command */

            byte b1 = buffer.get();
            sb.append(Character.toString((char) b1));

            if (depth == 1) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + String.format("%04x", b1 & 0xff));
                sb.append(System.lineSeparator());
            }

            if (depth == 2) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + String.format("%04x", b1 & 0xff));

                sb.append(System.lineSeparator()).append("\t").append("Bin: ");
                for (int i = 7; i >= 0; --i) sb.append(b1 >>> i & 1);
                sb.append(System.lineSeparator());
            }

           /* Length */

            short s0 = buffer.getShort();
            sb.append(Short.valueOf(s0));

            if (depth == 1) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + Integer.toHexString(s0 & 0xffff));
                sb.append(System.lineSeparator());
            }

            if (depth == 2) {
                sb.append(System.lineSeparator());
                sb.append("\tHex: " + Integer.toHexString(s0 & 0xffff));

                sb.append(System.lineSeparator());
                sb.append("\tBin: " + Integer.toBinaryString(0xFFFF & s0));
                sb.append(System.lineSeparator());
            }

            /* Data */


            /*
                Different logic for request BUFFER because it contains packet #
                instead of data length
             */
            if (b0 == '>' && b1 == 'B') {

                sb.append("{Packet: " + Short.valueOf(s0) + "}");

                if (depth == 0) sb.append("\t");

                if (depth == 1) {

                    /* Packet # */
                    sb.append(System.lineSeparator());
                    sb.append("\t< packet:");
                    sb.append(System.lineSeparator());
                    sb.append("\tHex: " + Integer.toHexString(s0 & 0xffff));

                }

                if (depth == 2) {

                    /* Packet # */
                    sb.append(System.lineSeparator());
                    sb.append("\t< packet:");
                    sb.append(System.lineSeparator());
                    sb.append("\t\tHex: " + Integer.toHexString(s0 & 0xffff));
                    sb.append(System.lineSeparator());
                    sb.append("\t\tBin: " + Integer.toBinaryString(0xFFFF & s0));

                }

                sb.append(System.lineSeparator()).append("-----------------------------------------------------");
                return sb.toString();

            }

            switch (s0) {
                case 0:

                    sb.append("{NO DATA}");

                    break;
                case 1:

                    byte b2 = buffer.get();
                    sb.append("{x" + String.format("%04x", b2 & 0xff) + "}");

                    if (depth == 1) {
                        sb.append(System.lineSeparator());
                        sb.append("\t< status|version:").append(System.lineSeparator());
                        sb.append("\t\tHex: " + String.format("%04x", b2 & 0xff));
                    }

                    if (depth == 2) {
                        sb.append(System.lineSeparator());
                        sb.append("\t< status|version:").append(System.lineSeparator());
                        sb.append("\t\tHex: " + String.format("%04x", b2 & 0xff));
                        sb.append(System.lineSeparator()).append("\t\t").append("Bin: ");
                        for (int i = 7; i >= 0; --i) sb.append(b2 >>> i & 1);
                    }

                    break;
                case 2:

                    short s1 = buffer.getShort();
                    sb.append("{" + Short.valueOf(s0) + "}");

                    if (depth == 1) {

                        sb.append(System.lineSeparator());
                        sb.append("\t> interval|voltage:").append(System.lineSeparator());
                        sb.append("\t\tHex: " + Integer.toHexString(s1 & 0xffff));
                    }

                    if (depth == 2) {
                        sb.append(System.lineSeparator());
                        sb.append("\t> interval|voltage:").append(System.lineSeparator());
                        sb.append("\t\tHex: " + Integer.toHexString(s1 & 0xffff));

                        sb.append(System.lineSeparator());
                        sb.append("\t\tBin: " + Integer.toBinaryString(0xFFFF & s1));
                    }

                    break;
                default: // BUFFER command

                    if (s0 >= 11) {

                        byte buf_b0 = buffer.get(); // Status

                        short buf_s0 = buffer.getShort();  // Temperature

                        byte buf_b1 = buffer.get(); // Current gain factor
                        byte buf_b2 = buffer.get(); // Voltage gain factor

                        /* Current check */

                        byte buf_b3 = buffer.get();
                        byte buf_b4 = buffer.get();
                        byte buf_b5 = buffer.get();

                        int current = (buf_b3 & 0xFF) | ((buf_b4 & 0xFF) << 8) | ((buf_b5 & 0xFF) << 16);

                        /* Voltage check */

                        byte buf_b6 = buffer.get();
                        byte buf_b7 = buffer.get();
                        byte buf_b8 = buffer.get();

                        int voltage = (buf_b6 & 0xFF) | ((buf_b7 & 0xFF) << 8) | ((buf_b8 & 0xFF) << 16);

                        sb.append("{Status: x" + String.format("%04x", buf_b0 & 0xff) + " | ");
                        sb.append("Temperature: " + Short.valueOf(buf_s0) + " | ");
                        sb.append("Current gain factor: " + Byte.valueOf(buf_b1) + " | ");
                        sb.append("Voltage gain factor: " + Byte.valueOf(buf_b2) + " | ");
                        sb.append("[check block-1] > Current check: " + Integer.valueOf(current) + " & ");
                        sb.append("Voltage check: " + Integer.valueOf(voltage) + " | ");
                        sb.append("[check block-2...] remaining length " + (s0 - 11) + " bytes = " + (s0 - 11) / 6 + " blocks}");

                        if (depth == 0) sb.append("\t");

                        if (depth == 1) {

                            /* Status */
                            sb.append(System.lineSeparator());
                            sb.append("\t< status:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b0 & 0xff));

                            /* Temperature */
                            sb.append(System.lineSeparator());
                            sb.append("\t< temperature:");
                            sb.append(System.lineSeparator());
                            sb.append("\tHex: " + Integer.toHexString(buf_s0 & 0xffff));

                            /* Current gain factor */
                            sb.append(System.lineSeparator());
                            sb.append("\t< current gain factor:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b1 & 0xff));

                            /* Voltage gain factor */
                            sb.append(System.lineSeparator());
                            sb.append("\t< voltage gain factor:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b2 & 0xff));

                            /* [Check block 1] */

                            sb.append(System.lineSeparator());
                            sb.append("\t[check block-1]");

                            /* Current check */

                            sb.append(System.lineSeparator());
                            sb.append("\t\t< current check:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\t\tHex: " + String.format("%04x", buf_b3 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b4 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b5 & 0xff));

                            /* Voltage check */

                            sb.append(System.lineSeparator());
                            sb.append("\t\t< voltage check:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\t\tHex: " + String.format("%04x", buf_b6 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b7 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b8 & 0xff));

                            /* [Check block 2] */

                            sb.append(System.lineSeparator());
                            sb.append("\t[check block-2...] remaining length " + (s0 - 11) + " bytes = " + (s0 - 11) / 6 + " blocks");

                        }

                        if (depth == 2) {

                            /* Status */
                            sb.append(System.lineSeparator());
                            sb.append("\t< status:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b0 & 0xff));
                            sb.append(System.lineSeparator()).append("\t\t").append("Bin: ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b0 >>> i & 1);

                            /* Temperature */
                            sb.append(System.lineSeparator());
                            sb.append("\t< temperature:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + Integer.toHexString(buf_s0 & 0xffff));
                            sb.append(System.lineSeparator());
                            sb.append("\t\tBin: " + Integer.toBinaryString(0xFFFF & buf_s0));

                            /* Current gain factor */
                            sb.append(System.lineSeparator());
                            sb.append("\t< current gain factor:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b1 & 0xff));
                            sb.append(System.lineSeparator()).append("\t\t").append("Bin: ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b1 >>> i & 1);

                            /* Voltage gain factor */
                            sb.append(System.lineSeparator());
                            sb.append("\t< voltage gain factor:");
                            sb.append(System.lineSeparator());
                            sb.append("\t\tHex: " + String.format("%04x", buf_b2 & 0xff));
                            sb.append(System.lineSeparator()).append("\t\t").append("Bin: ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b2 >>> i & 1);

                            /* [Check block 1] */

                            sb.append(System.lineSeparator());
                            sb.append("\t[check block-1]");

                            /* Current check */

                            sb.append(System.lineSeparator());
                            sb.append("\t\t< current check:");

                            sb.append(System.lineSeparator());
                            sb.append("\t\t\tHex: " + String.format("%04x", buf_b3 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b4 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b5 & 0xff));

                            sb.append(System.lineSeparator()).append("\t\t\t").append("Bin: ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b3 >>> i & 1);
                            sb.append(" | ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b4 >>> i & 1);
                            sb.append(" | ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b5 >>> i & 1);

                            sb.append(System.lineSeparator());
                            sb.append("\t\t\t\tCurrent check INTEGER (bin): ").append(Integer.toBinaryString(current));

                            /* Voltage check */

                            sb.append(System.lineSeparator());
                            sb.append("\t\t< voltage check:");

                            sb.append(System.lineSeparator());
                            sb.append("\t\t\tHex: " + String.format("%04x", buf_b6 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b7 & 0xff));
                            sb.append(" | ");
                            sb.append(String.format("%04x", buf_b8 & 0xff));

                            sb.append(System.lineSeparator()).append("\t\t\t").append("Bin: ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b6 >>> i & 1);
                            sb.append(" | ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b7 >>> i & 1);
                            sb.append(" | ");
                            for (int i = 7; i >= 0; --i) sb.append(buf_b8 >>> i & 1);

                            sb.append(System.lineSeparator());
                            sb.append("\t\t\t\tVoltage check INTEGER (bin): ").append(Integer.toBinaryString(voltage));

                            /* [Check block 2] */

                            sb.append(System.lineSeparator());
                            sb.append("\t[check block-2...] remaining length " + (s0 - 11) + " bytes = " + (s0 - 11) / 6 + " blocks");

                        }

                    }

                    break;

            }

            /*
                Source:
                http://stackoverflow.com/questions/13154715/convert-3-bytes-to-int-in-java

                int result = (buf_b1 & 0xFF) | ((buf_b2 & 0xFF) << 8) | ((buf_b3 & 0xFF) << 16);
                int result = ((buf_b3 & 0xFF) << 16) | ((buf_b2 & 0xFF) << 8) | (buf_b1 & 0xFF);

                http://stackoverflow.com/questions/14583442/java-bytebuffer-convert-three-bytes-to-int

                BIG_ENDIAN
                private static int toInt( byte[] b ) {
                    return (b[0] & 255) << 16 | (b[1] & 255) << 8 | (b[2] & 255);
                }

                LITTLE_ENDIAN
                private static int toInt( byte[] b ) {
                    return (b[2] & 255) << 16 | (b[1] & 255) << 8 | (b[0] & 255);
                }
            */

        } catch (Exception e) {
            sb.append(System.lineSeparator());
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            sb.append(errors.toString());
            sb.append(System.lineSeparator());
        }

        sb.append(System.lineSeparator()).append("-----------------------------------------------------");
        return sb.toString();

    }

    @Deprecated
    static String toBinaryString(ByteBuffer buffer) {

        if (buffer == null) return null;

        StringBuilder sb = new StringBuilder();

        buffer.position(0);

        byte b1 = buffer.get();
        sb.append("Direction: " + b1).append(System.lineSeparator());
        String s1 = "\tChar " + b1 + " (hex/bin): " + String.format("%04x", (int) b1) + " / "
                + String.format("%08d", Long.parseLong(Integer.toBinaryString((int) b1), 2));
        sb.append(s1).append(System.lineSeparator());

        byte b2 = buffer.get();
        sb.append("Command: " + b2).append(System.lineSeparator());
        String s2 = "\tChar " + b2 + " (hex/bin): " + String.format("%04x", (int) b2) + " / "
                + String.format("%08d", Long.parseLong(Integer.toBinaryString((int) b2), 2));
        sb.append(s2).append(System.lineSeparator());

        short sh0 = buffer.getShort();
        sb.append("Length: " + sh0).append(System.lineSeparator());
        String s3 = "\tShort " + sh0 + " (hex/bin): " + String.format("%04x", (int) sh0) + " / "
                + String.format("%08d", Long.parseLong(Integer.toBinaryString((int) sh0), 2));
        sb.append(s3).append(System.lineSeparator());

        sb.append(System.lineSeparator());

        if (buffer.remaining() == 1) {
            sb.append("Data: (1 byte) " + buffer.get()).append(System.lineSeparator());
        }

        if (buffer.remaining() == 2) {
            sb.append("Data: (1 short) " + buffer.getShort()).append(System.lineSeparator());
        }

/*
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            String s = "Char " + b + " (hex/bin): " + String.format("%04x", (int) b) + " / "
                    + String.format("%08d", Long.parseLong(Integer.toBinaryString((int) b), 2));

//            http://stackoverflow.com/questions/14012013/java-converting-negative-binary-back-to-integer
//            String s = "Char " + b + " (hex/bin): " + String.format("%04x", (int) b) + " / "
//                    + String.format("%08d", Integer.parseUnsignedInt(Integer.toBinaryString((int) b)));

            sb.append(s).append(System.lineSeparator());
        }
*/

        return sb.toString();

    }

}
