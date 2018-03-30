package com.itsm.controller.data.dto;

import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.Command;

/**
 * Created by anpiakhota on 22.10.16.
 */

public abstract class AbstractInteractionFactory {

    public abstract Request getRequest(Command command);
    public abstract Response getResponse(Request request);

}
