package com.itsm.controller.data.dto.response;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.packet.PacketParserException;
import com.itsm.controller.data.packet.Specification;

import java.nio.ByteBuffer;

/**
 * Created by anpiakhota on 22.10.16.
 */

public interface Response extends Specification {

    /**
     * The main method that parses responseBuffer (byte data) from controller
     * and initializes a response object.
     * @param responseBuffer
     * @throws PacketParserException
     */
    void parse(ByteBuffer responseBuffer) throws PacketParserException;
    Request getRequest();
    String toResponseMonitorString(ByteBuffer responseBuffer, int depth);
    String getResponseMonitorString();
    Data getData();

}
