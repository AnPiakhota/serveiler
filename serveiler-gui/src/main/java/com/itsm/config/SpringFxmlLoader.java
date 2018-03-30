package com.itsm.config;

import javafx.fxml.FXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Source: http://www.oracle.com/technetwork/articles/java/zonski-1508195.html
 */
@Component
public class SpringFxmlLoader {

    @Autowired
    private ApplicationContext applicationContext;

    public Object load(String url, Class<?> controllerClass) throws IOException {
        try (InputStream fxmlStream = controllerClass.getClassLoader().getResourceAsStream(url)) {
            Object instance = applicationContext.getBean(controllerClass);
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(applicationContext::getBean);
            loader.getNamespace().put("controller", instance);
            loader.setLocation(controllerClass.getClassLoader().getResource(url));
            return loader.load(fxmlStream);
        }
    }

    public Object load(String url, Class<?> controllerClass, Object root) throws IOException {
        try (InputStream fxmlStream = controllerClass.getClassLoader().getResourceAsStream(url)) {
            Object instance = applicationContext.getBean(controllerClass);
            FXMLLoader loader = new FXMLLoader();
            loader.setRoot(root);
            loader.setControllerFactory(applicationContext::getBean);
            loader.getNamespace().put("controller", instance);
            loader.setLocation(controllerClass.getClassLoader().getResource(url));
            return loader.load(fxmlStream);
        }

    }

//        Another variant
//        FXMLLoader loader = new FXMLLoader();
//        loader.setControllerFactory(applicationContext :: getBean);
//        loader.setLocation(getClass().getClassLoader().getResource(url));
//        return loader.load();

}

