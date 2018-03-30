package com.itsm.controller.data.dto.request.impl;

import com.itsm.controller.data.dto.request.AbstractRequest;
import com.itsm.controller.data.packet.Command;

import java.nio.ByteBuffer;

/**
 * Created by anpiakhota on 20.10.16.
 */

public class ActionRequest extends AbstractRequest {

    public ActionRequest(Command command) {
        super(command);
    }

    @Override
    public void put(ByteBuffer requestBuffer) {

        requestBuffer.put(direction).put(token);

        switch (command) {
            case START:
                super.length = 2;
                requestBuffer.putShort(length).putShort(data.checkInterval);
                break;
            case STOP:
                requestBuffer.putShort(length);
                break;
            case OUTPUT_VOLTAGE:
                super.length = 2;
                requestBuffer.putShort(length).putChar(data.outputVoltage);
                break;
            case GAIN_FACTOR:
                super.length = 2;
                requestBuffer.putShort(length).put(data.currentGainFactor).put(data.voltageGainFactor);
                break;

        }

    }

}
