package com.itsm.controller.data.dto;

import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.dto.response.impl.*;
import com.itsm.controller.data.packet.Command;

/**
 * Created by anpiakhota on 22.10.16.
 */

public class ResponseFactory extends AbstractInteractionFactory {

    @Override
    public Request getRequest(Command command) {
        return null;
    }

    @Override
    public Response getResponse(Request request) {

        switch (request.getCommand()) {

            /* INFO */

            case PING:
                return new InfoResponse(request);
            case VERSION:
                return new InfoResponse(request);

            /* ACTION */

            case START:
                return new ActionResponse(request);
            case STOP:
                return new ActionResponse(request);
            case OUTPUT_VOLTAGE:
                return new ActionResponse(request);
            case GAIN_FACTOR:
                return new ActionResponse(request);

            /* READ */

            case BUFFER:
                return new ReadResponse(request);

            default:
                return null;

        }

    }

}
