package com.itsm.controller.data.dto.response;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.packet.Specification;

import java.nio.ByteBuffer;

/**
 * Created by anpiakhota on 20.12.16.
 */
public abstract class AbstractResponse implements Response {

    protected Command command;
    protected Request request;

    /* Packet */
    protected byte direction;
    protected byte token;
    protected short length;
    protected Data data;

    /* Monitor */
    protected String responseMonitorString;

    public AbstractResponse(Request request) {
        this.request = request;
        this.command = request.getCommand();
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public String toResponseMonitorString(ByteBuffer responseBuffer, int depth) {
        responseMonitorString = Specification.toMonitorString(responseBuffer, depth);
        return responseMonitorString;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public String getResponseMonitorString() {
        return responseMonitorString == null ? "" : responseMonitorString;
    }

}
