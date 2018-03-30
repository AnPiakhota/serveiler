package com.itsm.controller.data.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anpiakhota on 15.12.16.
 */
@Deprecated
public class Packet implements ISpec {

    private char direction;
    private char command;
    /**
     * Packet length in a row of the same commands
     */
    private short length;
    private byte[] data;

    private Packet(PacketBuilder builder) {
        this.direction = DIRECTION_TO;
        this.command = builder.command;
        this.length = builder.length;

//        if (builder.dataValues.size() != 0) {
//            String d = builder.dataValues.stream().map(e -> e.toString()).reduce(",", String::concat);
//            this.data = DATA_GROUP.replace("+", d);
//        } else {
//            this.data = DATA_GROUP.replace("+", "");
//        }

    }

    private String toPacket() {

        StringBuilder sb = new StringBuilder();
        sb.append(direction)
          .append(command)
          .append(length)
          .append(data);

        return sb.toString();
    }

    public char getCommand() {
        return command;
    }

    public byte[] toBytes() {
        return null;
    }

    public static class PacketBuilder {

        private char command;
        private short length;
        private byte[] data;

        public PacketBuilder(char command) {
            this.command = command;
        }

        public PacketBuilder lenght(short length) {
            this.length = length;
            return this;
        }

        public PacketBuilder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Packet build() {
            return new Packet(this);
        }


        List<String> dataValues = new ArrayList<>();
        @Deprecated
        public PacketBuilder dataValue(char type, Object value) {

            String str = null;

            if (value instanceof Integer) str = Integer.toString((int) value);
            if (value instanceof Long) str = Long.toString((long) value);
            if (value instanceof Float) str = Float.toString((float) value);
            if (value instanceof Double) str = Double.toString((double) value);
            if (value instanceof Character) str = Character.toString((char) value);
            if (value instanceof String) str = (String) value;

            dataValues.add(type + str);

            return this;

        }

    }

    public static class PacketModifier {

        private Packet packet;

        public PacketModifier modify(Packet packet) {
            this.packet = packet;
            return this;
        }

        public PacketModifier length(short length) {
            this.packet.length = length;
            return this;
        }

        public Packet build() {
            return packet;
        }

    }

    public static class PacketParser {

        private char command;
        private short length;

        /* Data */
        private char status; // Ping | Confirmation status
        private char version;

        private double temperature;

        /**
         * Amperage in micro-amperes with two numbers after decimal point
         */
        private double[] current;

        /**
         * Voltage in value in millivolts with one number after decimal point
         */
        private double[] voltage;

        public Packet parse(byte[] data) throws PacketParserException {

            // TODO

            return new Packet.PacketBuilder('V').build();

        }

        @Deprecated private Map<String, String> groups = new HashMap<>();
        @Deprecated public Map<String, String> getGroups() {
            return groups;
        }

        @Deprecated
        public void parse(String packet) throws PacketParserException {
            groups.clear();
            if (!validate(packet)) throw new PacketParserException("Validation failed");
        }

        @Deprecated
        private boolean validate(String packet) {
            Pattern pattern = Pattern.compile(packetPattern);
            Matcher matcher = pattern.matcher(packet);

            if (matcher.matches()) {
                groups.put("command", matcher.group("command"));
                groups.put("length", matcher.group("length"));
                groups.put("data", matcher.group("data"));
                return true;
            } else return false;

        }

    }

}
