package com.itsm.controller.instance;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.InteractionFactoryProducer;

/**
 * Created by anpiakhota on 13.12.16.
 */
public class BaseController<T extends ComAPI> implements AutoCloseable {

    protected T api;

    @Override
    public void close() throws ControllerCommunicationException {
        if (api != null) api.closePort();
    }

}
