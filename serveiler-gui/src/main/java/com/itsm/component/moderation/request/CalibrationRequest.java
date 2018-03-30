package com.itsm.component.moderation.request;

import com.itsm.component.moderation.ModerationListener;
import com.itsm.worker.ModerationTask;
import com.itsm.worker.FileWorker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Source: https://docs.oracle.com/javase/8/javafx/fxml-tutorial/custom_control.htm
 *
 *
 */
public class CalibrationRequest extends GridPane {

    private int type;
    private String sType;

    public static final int CURRENT = ModerationTask.CURRENT;
    public static final int VOLTAGE = ModerationTask.VOLTAGE;
    public static final int TEMPERATURE = ModerationTask.TEMPERATURE;

    private double parameter1;
    private double parameter2;
    private double response1;
    private double response2;
    private double coefficient1;
    private double coefficient2;

    @FXML private GridPane calibrationRequestPane;
    @FXML private Label labelOrder;
    @FXML private ImageView imageViewAction;
    @FXML private TextField textFieldArgument;
    @FXML private Button buttonRequest;

    @Autowired
    private FileWorker fileWorker;

    @Autowired
    private ApplicationContext applicationContext;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ModerationTask moderationTask1;
    private ModerationTask moderationTask2;

    private ModerationListener moderationListener;


    /*
        Source: http://www.vipan.com/htdocs/bitwisehelp.html
    */

    /**
     * The flag is set while processing.
     */
    private final int PROCESSING = 1 << 0; // 1

    private final int PROCESSING_PARAMETER1 = 1 << 1; // 2

    private final int PROCESSING_PARAMETER2 = 1 << 2; // 4

    private final int COMPLETED_PARAMETER1 = 1 << 3; // 8

    private final int COMPLETED_PARAMETER2 = 1 << 4; // 16

    private volatile int flags = 0;
    private ProgressBar progressBar;
    private int rowIndex;

    public CalibrationRequest() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "moderation-request-calibration.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }


    public CalibrationRequest initialize(ModerationListener listener, int index) {

        this.moderationListener = listener;
        this.rowIndex = index;

        progressBar = new ProgressBar(0);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.prefWidthProperty().bind(textFieldArgument.widthProperty());
        progressBar.prefHeightProperty().bind(textFieldArgument.heightProperty());

        return this;

    }

    public void configTask(int type) {

        this.type = type;

        flags |= PROCESSING_PARAMETER1;

        moderationTask1 = applicationContext.getBean(ModerationTask.class);
        moderationTask2 = applicationContext.getBean(ModerationTask.class);

        short calibrationCheckInterval = Short.parseShort(fileWorker.getConfigProperties()
                .getProperty(fileWorker.PROP_CALIBRATION_CHECK_INTERVAL));

        switch (type) {
            case CURRENT:
                sType = "CURRENT";
                moderationTask1.config(CURRENT, calibrationCheckInterval);
                moderationTask2.config(CURRENT, calibrationCheckInterval);
                break;
            case VOLTAGE:
                sType = "VOLTAGE";
                moderationTask1.config(VOLTAGE, calibrationCheckInterval);
                moderationTask2.config(VOLTAGE, calibrationCheckInterval);
                break;
            case TEMPERATURE:
                sType = "TEMPERATURE";
                moderationTask1.config(TEMPERATURE, calibrationCheckInterval);
                moderationTask2.config(TEMPERATURE, calibrationCheckInterval);
                break;
        }

        /* Task 1 */

        moderationTask1.setOnSucceeded(t -> {

            flags ^= PROCESSING;
            flags ^= PROCESSING_PARAMETER1;
            flags |= COMPLETED_PARAMETER1;

            response1 = moderationTask1.getValue();

            moderationListener.log("Processing is successfully completed " +
                    "for parameter 1 with response: " + response1);

            labelOrder.setText("2");
            textFieldArgument.clear();
            textFieldArgument.setVisible(true);
            calibrationRequestPane.getChildren().remove(progressBar);
            textFieldArgument.setPromptText("Enter calibration parameter 2");

        });

        moderationTask1.setOnFailed(t -> onTaskFailed(1, (Exception) moderationTask1.getException()));

        /* Task 2 */

        moderationTask2.setOnSucceeded(t -> {

            flags ^= PROCESSING;
            flags ^= PROCESSING_PARAMETER2;
            flags |= COMPLETED_PARAMETER2;

            response2 = moderationTask2.getValue();

            moderationListener.log("Processing is successfully completed " +
                    "for parameter 2 with response: " + response2);

            moderationListener.log("Calculating moderation coefficients");
            calibrate();

            Properties properties = fileWorker.getConfigProperties();

            switch (type) {
                case CURRENT:
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF1, Double.toString(coefficient1));
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF2, Double.toString(coefficient2));
                    break;
                case VOLTAGE:
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF1, Double.toString(coefficient1));
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF2, Double.toString(coefficient2));
                    break;
                case TEMPERATURE:
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF1, Double.toString(coefficient1));
                    properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF2, Double.toString(coefficient2));
                    break;
            }

            if (!fileWorker.storeConfigProperties(properties)) {
                moderationListener.log("Storing data into serveiler.properties file has been failed.");
            }

            flags = 0;
            flags |= PROCESSING_PARAMETER1;

            moderationListener.refresh(rowIndex);

        });

        moderationTask2.setOnFailed(t -> {
            onTaskFailed(2, (Exception) moderationTask2.getException());
        });

    }

    protected void onTaskFailed(int pointer, Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        moderationListener.log(sw.toString());

        labelOrder.setText(String.valueOf(pointer));
        textFieldArgument.setVisible(true);
        calibrationRequestPane.getChildren().remove(progressBar);
        textFieldArgument.setPromptText("Enter calibration parameter " + pointer);

        flags ^= PROCESSING;

        if (pointer == 1) {
            flags &= ~PROCESSING_PARAMETER1;
            flags &= ~COMPLETED_PARAMETER1;
        }

        if (pointer == 2) {
            flags &= ~PROCESSING_PARAMETER2;
            flags &= ~COMPLETED_PARAMETER2;
        }

    }

    protected void calibrate() {

        coefficient1 = (parameter2 - parameter1) / (response2 - response1);

        moderationListener.log("coefficient1 = (parameter2 - parameter1) / (response2 - response1)");
        moderationListener.log("coefficient1 = " + coefficient1);

        coefficient2 = parameter1 - coefficient1 * response1;

        moderationListener.log("coefficient2 = parameter1 - coefficient1 * response1");
        moderationListener.log("coefficient2 = " + coefficient2);

    }

    @FXML
    protected void request() {

        if ((flags & PROCESSING) == PROCESSING) {
            moderationListener.log("Operation is not allowed while processing");
            return;
        }

        double input;
        try {
            input = convertParameter(textFieldArgument.getText());
        } catch(NumberFormatException e) {
            moderationListener.log("Failure while formatting entered value to the number");
            return;
        }

        flags |= PROCESSING;

        textFieldArgument.setVisible(false);
        calibrationRequestPane.add(progressBar, 1, 0, 2, 1);

        if ((flags & PROCESSING_PARAMETER1) == PROCESSING_PARAMETER1) {
            parameter1 = input;

            moderationListener.log("Processing is started for "
                    + sType + " parameter 1: " + parameter1);

            executorService.submit(moderationTask1);

        }

        if ((flags & COMPLETED_PARAMETER1) == COMPLETED_PARAMETER1) {
            flags |= PROCESSING_PARAMETER2;
            parameter2 = input;

            moderationListener.log("Processing is started for "
                    + sType + " parameter 2: " + parameter2);

            executorService.submit(moderationTask2);

        }

    }

    private double convertParameter(String input) throws NumberFormatException {
        if (!(input == null || input.length() == 0)) {
            return Double.parseDouble(input);
        } else throw new NumberFormatException();
    }

}