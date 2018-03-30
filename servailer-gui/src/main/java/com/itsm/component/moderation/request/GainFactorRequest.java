package com.itsm.component.moderation.request;

import com.itsm.component.moderation.ModerationListener;
import com.itsm.worker.ModerationTask;
import com.itsm.worker.FileWorker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
public class GainFactorRequest extends GridPane {

    private byte current;
    private byte voltage;

    @FXML private GridPane gainFactorRequestPane;
    @FXML private Label labelOrder;
    @FXML private ImageView imageViewAction;
    @FXML private ChoiceBox currentFactorChoice;
    @FXML private ChoiceBox voltageFactorChoice;
    @FXML private Button buttonRequest;

    private volatile boolean processing;

    @Autowired
    private FileWorker fileWorker;

    @Autowired
    private ApplicationContext applicationContext;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ModerationTask moderationTask;
    private ModerationListener moderationListener;

    private ProgressBar progressBar;
    private int rowIndex;

    public GainFactorRequest() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "moderation-request-gain-factor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    public GainFactorRequest initialize(ModerationListener listener, int index, String current, String voltage) {

        this.moderationListener = listener;
        this.rowIndex = index;

        if (current != null && !current.isEmpty()) {
            currentFactorChoice.setValue(current);
        }

        if (voltage != null && !voltage.isEmpty()) {
            voltageFactorChoice.setValue(voltage);
        }

        progressBar = new ProgressBar(0);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.prefWidthProperty().bind(currentFactorChoice.widthProperty());
        progressBar.prefHeightProperty().bind(currentFactorChoice.heightProperty());

        return this;

    }

    public void configTask() {

        moderationTask = applicationContext.getBean(ModerationTask.class);

        /* Task */

        moderationTask.setOnSucceeded(t -> {

            processing = false;

            moderationListener.log("Processing is successfully completed for setting gain factor");

            Properties properties = fileWorker.getConfigProperties();
            properties.setProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_CURRENT, Byte.toString(current));
            properties.setProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_VOLTAGE, Byte.toString(voltage));
            if (!fileWorker.storeConfigProperties(properties)) {
                moderationListener.log("Storing data into serveiler.properties file has been failed.");
            }

            moderationListener.refresh(rowIndex);

        });

        moderationTask.setOnFailed(t -> onTaskFailed((Exception) moderationTask.getException()));

    }

    protected void onTaskFailed(Exception e) {

        processing = false;

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        moderationListener.log(sw.toString());

        currentFactorChoice.setVisible(true);
        voltageFactorChoice.setVisible(true);
        gainFactorRequestPane.getChildren().remove(progressBar);

    }

    @FXML
    protected void request() {

        if (processing) {
            moderationListener.log("Operation is not allowed while processing");
            return;
        }

        processing = true;

        current = Byte.parseByte((String) currentFactorChoice.getValue());
        voltage = Byte.parseByte((String) voltageFactorChoice.getValue());

        moderationTask.config(ModerationTask.GAIN_FACTOR, current, voltage);

        currentFactorChoice.setVisible(false);
        voltageFactorChoice.setVisible(false);
        gainFactorRequestPane.add(progressBar, 1, 0, 2, 1);

        executorService.submit(moderationTask);

        moderationListener.log("Processing is started for GAIN_FACTOR >>"
                + " current: " + current + "; voltage: " + voltage);

    }

}