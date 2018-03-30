package com.itsm;

import com.itsm.config.SpringConfiguration;
import com.itsm.config.gui.StageConfiguration;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main extends Application {

    private AnnotationConfigApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /*
            http://steveonjava.com/javafx-and-spring-day-1/
            https://github.com/steveonjava/JavaFX-Spring/blob/master/client/src/main/java/steveonjava/client/CustomerApp.java
         */
        context = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        StageConfiguration stageConfiguration = context.getBean(StageConfiguration.class);



/*
        http://stackoverflow.com/questions/19602727/how-to-reference-javafx-fxml-files-in-resource-folder
        FXMLLoader usage recommendations

        Instantiate an FXMLLoader via new FXMLLoader() rather than using the static methods on the FXMLLoader.
        The static methods become confusing when you want to get values (like instantiated controllers) out of a loader.
        Set the location on the instantiated FXMLLoader and call load() rather than loading from a stream using load(stream).
                Setting a URL based location on the loader allows for resolution of relative resources loaded in fxml and css files. Relative resources do not resolve for a stream based constructor.
        To derive a location based upon a class, use getClass().getResource(), as it is URL based, rather than getClass().getResourceAsStream() which is stream based.
*/

//        http://www.devlabs.ninja/article/font-awesome-icons-in-a-javafx-application
//        InputStream font = App.class.getResourceAsStream("/fa/fontawesome-webfont.ttf");
//        Font fontAwesome = Font.loadFont(font, 10);

        String version = com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();

        stageConfiguration.initialize(primaryStage);

    }

    @Override
    public void stop() throws Exception {
        context.close();
    }

}
