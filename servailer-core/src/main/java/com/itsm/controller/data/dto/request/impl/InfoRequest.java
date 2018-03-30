package com.itsm.controller.data.dto.request.impl;

import com.itsm.controller.data.dto.request.AbstractRequest;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.dto.request.Request;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by anpiakhota on 20.10.16.
 */
public class InfoRequest extends AbstractRequest {

    public InfoRequest(Command command) {
        super(command);
    }

    @Override
    public void put(ByteBuffer requestBuffer) {
        requestBuffer.put(direction).put(token).putShort(length);
    }

}
