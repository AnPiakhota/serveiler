package com.itsm.controller.data.dto.response.impl;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.AbstractResponse;
import com.itsm.controller.data.packet.PacketParserException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by anpiakhota on 20.10.16.
 */
public class InfoResponse extends AbstractResponse {

    public InfoResponse(Request request) {
        super(request);
    }

    @Override
    public void parse(ByteBuffer responseBuffer) throws PacketParserException {

        if (responseBuffer == null) return;

        super.direction = responseBuffer.get();
        super.token = responseBuffer.get();
        super.length = responseBuffer.getShort();

        super.data = new Data();

        switch (command) {
            case PING:
                super.data.pingStatus = responseBuffer.get();
                break;
            case VERSION:
                super.data.firmwareVersion = responseBuffer.get();
                break;
        }

    }

}
