package com.itsm.config.gui;

import com.itsm.component.moderation.request.CalibrationRequest;
import com.itsm.component.moderation.request.GainFactorRequest;
import com.itsm.component.moderation.request.OutputVoltageRequest;
import com.itsm.config.SpringFxmlLoader;
import com.itsm.controller.ModerationController;
import com.itsm.controller.LineChartController;
import com.itsm.controller.ServeilerController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

/**
 * Created by anpiakhota on 22.12.16.
 */

@Configuration
@Lazy
public class StageConfiguration {

    @Autowired
    private SpringFxmlLoader springFxmlLoader;

    @Bean
    @Scope(value = "singleton")
    public SpringFxmlLoader springFxmlLoader(){
        return new SpringFxmlLoader();
    }

    private Stage primaryStage;

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void initialize(Stage stage) throws IOException {

        primaryStage = stage;

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icons/serveiler.png")));
        primaryStage.setTitle("Serveiler V.1");

        Parent root = (Parent) springFxmlLoader.load("serveiler.fxml", ServeilerController.class);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getClassLoader().getResource("serveiler-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

    }

    @Bean
    @Scope(value = "singleton")
    ServeilerController serveilerController() {
        return new ServeilerController();
    }

    @Bean
    @Scope(value = "singleton")
    ModerationController calibrationController() {
        return new ModerationController();
    }

    @Bean
    @Scope(value = "singleton")
    LineChartController lineChartController() {
        return new LineChartController();
    }

    @Bean
    @Scope(value = "prototype")
    CalibrationRequest calibrationRequest() {
        return new CalibrationRequest();
    }

    @Bean
    @Scope(value = "prototype")
    OutputVoltageRequest outputVoltageRequest() {
        return new OutputVoltageRequest();
    }

    @Bean
    @Scope(value = "prototype")
    GainFactorRequest gainFactorRequest() {
        return new GainFactorRequest();
    }

}
