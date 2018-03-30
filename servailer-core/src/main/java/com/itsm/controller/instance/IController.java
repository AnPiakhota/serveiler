package com.itsm.controller.instance;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.PacketParserException;

/**
 * Created by anpiakhota on 13.12.16.
 */
public interface IController <T extends ComAPI> {

    String[] listAvailablePorts() throws ControllerCommunicationException;
    Response request(Request request) throws ControllerCommunicationException, PacketParserException;

}
