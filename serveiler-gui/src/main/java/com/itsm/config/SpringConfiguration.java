package com.itsm.config;

import com.itsm.Main;
import com.itsm.config.gui.StageConfiguration;
import com.itsm.config.request.RequestConfiguration;
import com.itsm.worker.ModerationTask;
import com.itsm.worker.FileWorker;
import com.itsm.worker.LoadService;
import com.itsm.worker.LoadTask;
import javafx.scene.text.Font;
import org.springframework.context.annotation.*;

/**
 * Source: http://docs.spring.io/spring-javaconfig/docs/1.0.0.m3/reference/html/modularizing-configurations.html
 */
@Configuration
@Import({StageConfiguration.class, RequestConfiguration.class})
public class SpringConfiguration {

    static {
//        http://www.jensd.de/wordpress/?p=132
//        http://fontawesome.io/
        Font.loadFont(Main.class.getResource("/fonts/fontawesome-webfont.ttf").toExternalForm(), 10);
//        Font.loadFont(Main.class.getResource("/fonts/bootstrap-3.3.7/glyphicons-halflings-regular.ttf").toExternalForm(), 10);

    }

    @Bean
    public FileWorker fileWorker() {
        return new FileWorker();
    }

    @Bean
    @Scope(value = "prototype")
    public ModerationTask calibrationTask() {
        return new ModerationTask();
    }

    @Bean
    @Scope(value = "prototype")
    public LoadTask loadTask() {
        return new LoadTask();
    }

    @Bean
    public LoadService loadService() {
        return new LoadService();
    }

}
