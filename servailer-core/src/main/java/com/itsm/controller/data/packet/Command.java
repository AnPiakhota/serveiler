package com.itsm.controller.data.packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Created by anpiakhota on 22.10.16.
 */
public enum Command {

    /**
     * Type: Info
     */
    PING('P', 0, 4),

    /**
     * Type: Info
     */
    VERSION('V', 0, 4),

    /**
     * Type: Action
     */
    START('S', 2, 6),

    /**
     * Type: Action
     */
    STOP('X', 0, 4),

    /**
     * Type: Action
     */
    OUTPUT_VOLTAGE('D', 2, 6),

    /**
     * Type: Action
     */
    GAIN_FACTOR('G', 2, 6),

    /**
     * Type: Read
     */
    BUFFER('B', 0, 4);

    private final char token;
    private final int requestLength;
    private final int requestBufferAllocation;

    Command(char token, int lenght, int allocation) {
        this.token = token;
        this.requestLength = lenght;
        this.requestBufferAllocation = allocation;
    }

    public char getToken() {
        return token;
    }

    public byte getTokenBinary() {
        return (byte) token;
    }

    public int getRequestBufferAllocation() {
        return requestBufferAllocation;
    }

    public short getRequestLength() {
        return (short) Math.min(Math.max(requestLength, Short.MIN_VALUE), Short.MAX_VALUE);
    }

    @Deprecated
    public static Command byToken(int c) {
        for (Command m : Command.values()) {
            if (m.token == c) {
                return m;
            }
        }
        return null;
    }

    public static Command getCommand(final int code) {
        return sMap.get(code);
    }

    private static final Map<Integer, Command> sMap = Collections.unmodifiableMap(createMap(Command::getToken, Command.class));

    public static <E extends Enum<E>> Map<Integer, E> createMap(final ToIntFunction<E> converter, final Class<E> enumClass) {

        final Map<Integer, E> map = new HashMap<>();
        for (final E s : enumClass.getEnumConstants()) {

            map.put(converter.applyAsInt(s), s);
        }
        return map;
    }

}
