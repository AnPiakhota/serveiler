package com.itsm.controller.data.dto;

import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.request.impl.ActionRequest;
import com.itsm.controller.data.dto.request.impl.InfoRequest;
import com.itsm.controller.data.dto.request.impl.ReadRequest;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.Command;

/**
 * Created by anpiakhota on 22.10.16.
 */

public class RequestFactory extends AbstractInteractionFactory {

    @Override
    public Request getRequest(Command command) {

        switch (command) {

            /* INFO */

            case PING:
                return new InfoRequest(Command.PING);
            case VERSION:
                return new InfoRequest(Command.VERSION);

            /* ACTION */

            case START:
                return new ActionRequest(Command.START);
            case STOP:
                return new ActionRequest(Command.STOP);
            case OUTPUT_VOLTAGE:
                return new ActionRequest(Command.OUTPUT_VOLTAGE);
            case GAIN_FACTOR:
                return new ActionRequest(Command.GAIN_FACTOR);

            /* READ */

            case BUFFER:
                return new ReadRequest(Command.BUFFER);

            default:
                return null;

        }

    }

    @Override
    public Response getResponse(Request request) {
        return null;
    }

}
