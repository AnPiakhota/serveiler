package com.itsm.config.request;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.ControllerFacade;
import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.bridge.impl.SerialPundit;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.InteractionFactoryProducer;
import com.itsm.controller.data.dto.response.Response;
import io.reactivex.subscribers.ResourceSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 * Source: https://blog.jayway.com/2014/02/16/spring-propertysource/
 *
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 * Properties can be bundled inside your jar that provides a sensible default name.
 * When running in production, an config.properties can be provided outside of jar
 * that overrides name; and for one off testing, you can launch with a specific command
 * line switch (e.g. java -jar app.jar --name="Spring").
 *
 */
@Configuration
@Lazy
@PropertySource("classpath:config/config.properties")
@PropertySource(value = "file:${user.home}/serveiler/serveiler.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.dir}/serveiler.properties", ignoreResourceNotFound = true)
public class RequestConfiguration {

    ControllerFacade.Controller controller;
    ComAPI api;

    @Autowired
    public RequestConfiguration(@Value("${serveiler.controller}") final String controller,
                                @Value("${serveiler.api}") final String api,
                                @Value("${serveiler.com.port}") final String port) throws ControllerCommunicationException {

        switch (controller) {
            case "CP2102": this.controller = ControllerFacade.Controller.CP2102; break;
        }

        switch (api) {
            case "SerialPundit": this.api = new SerialPundit(port); break;
        }

    }

    @Bean
    public ControllerFacade controllerFacade() {
        return new ControllerFacade(controller, api);
    }

    @Bean (name="requestFactory")
    public AbstractInteractionFactory abstractInteractionFactory() {
        return InteractionFactoryProducer.getFactory("REQUEST");
    }

}
