package com.itsm.controller.data.dto.request;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.packet.Specification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Supplier;

/**
 * Created by anpiakhota on 20.12.16.
 */
public abstract class AbstractRequest implements Request {

    protected Command command;

    /* Packet */
    protected byte direction = DIRECTION_TO;
    protected byte token;
    protected short length;
    protected short packetNumber;
    protected Data data;

    /* Monitor */
    protected String requestMonitorString;

    public AbstractRequest(Command command) {
        this.command = command;
        this.token = command.getTokenBinary();
        this.length = command.getRequestLength();
        this.packetNumber = 0;
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public String toRequestMonitorString(ByteBuffer requestBuffer, int depth) {
        requestMonitorString = Specification.toMonitorString(requestBuffer, depth);
        return requestMonitorString;
    }

    @Override
    public void setData(Supplier<Data> supplier) {
        this.data = supplier.get();
    }

    @Override
    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(command.getRequestBufferAllocation());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        packetNumber--; // Temporarily decrease packet number for it will be incremented in put method.
        put(buffer);
        return buffer;
    }

    @Override
    public byte[] toBytes() {
        return toBuffer().array();
    }

    @Override
    public String getRequestMonitorString() {
        return requestMonitorString == null ? "" : requestMonitorString;
    }

}
