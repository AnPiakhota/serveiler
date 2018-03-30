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
public class OutputVoltageRequest extends GridPane {

    private int constant1;
    private int constant2;
    private double voltage1;
    private double voltage2;
    private double coefficient1;
    private double coefficient2;

    @FXML private GridPane outputVoltageRequestPane;
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

    /* Steps */
    private final int RESET_PROCEDURE = 0;
    private final int REQUEST_CONSTANT1 = 1;
    private final int INPUT_VOLTAGE1 = 2;
    private final int REQUEST_CONSTANT2 = 3;
    private final int INPUT_VOLTAGE2 = 4;

    private volatile int step = REQUEST_CONSTANT1;

    private ProgressBar progressBar;
    private int rowIndex;
    private Properties properties;

    public OutputVoltageRequest() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "moderation-request-output-voltage.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    public OutputVoltageRequest initialize(ModerationListener listener, int index) {

        this.moderationListener = listener;
        this.rowIndex = index;

        properties = fileWorker.getConfigProperties();
        textFieldArgument.setText(properties.getProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_CONST1));

        progressBar = new ProgressBar(0);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.prefWidthProperty().bind(textFieldArgument.widthProperty());
        progressBar.prefHeightProperty().bind(textFieldArgument.heightProperty());

        return this;

    }

    public void configTask() {

        /* Task 1 */

        moderationTask1 = applicationContext.getBean(ModerationTask.class);

        moderationTask1.setOnSucceeded(t -> {

            step = INPUT_VOLTAGE1;
            moderationListener.log("Constant 1 has been set successfully");

            labelOrder.setText("2");
            textFieldArgument.clear();
            textFieldArgument.setVisible(true);
            outputVoltageRequestPane.getChildren().remove(progressBar);
            textFieldArgument.setPromptText("Enter output voltage 1");
            buttonRequest.setText("Save");
            buttonRequest.setDisable(false);

        });

        moderationTask1.setOnFailed(t -> {

            step = REQUEST_CONSTANT1;
            moderationListener.log("Failure while setting constant 1");

            StringWriter sw = new StringWriter();
            moderationTask1.getException().printStackTrace(new PrintWriter(sw));
            moderationListener.log(sw.toString());

            labelOrder.setText("1");
            textFieldArgument.clear();
            textFieldArgument.setVisible(true);
            outputVoltageRequestPane.getChildren().remove(progressBar);
            textFieldArgument.setPromptText("Enter constant value 1");
            buttonRequest.setDisable(false);

        });

        /* Task 2 */

        moderationTask2 = applicationContext.getBean(ModerationTask.class);

        moderationTask2.setOnSucceeded(t -> {

            step = INPUT_VOLTAGE2;
            moderationListener.log("Constant 2 has been set successfully");

            labelOrder.setText("4");
            textFieldArgument.clear();
            textFieldArgument.setVisible(true);
            outputVoltageRequestPane.getChildren().remove(progressBar);
            textFieldArgument.setPromptText("Enter output voltage 2");
            buttonRequest.setText("Calculate");
            buttonRequest.setDisable(false);

        });

        moderationTask2.setOnFailed(t -> {

            step = REQUEST_CONSTANT2;
            moderationListener.log("Failure while setting constant 2");

            StringWriter sw = new StringWriter();
            moderationTask2.getException().printStackTrace(new PrintWriter(sw));
            moderationListener.log(sw.toString());

            labelOrder.setText("3");
            textFieldArgument.clear();
            textFieldArgument.setVisible(true);
            outputVoltageRequestPane.getChildren().remove(progressBar);
            textFieldArgument.setPromptText("Enter constant value 2");


        });

    }

    @FXML
    protected void request() {

        buttonRequest.setDisable(true);

        switch (step) {
            case RESET_PROCEDURE:



            case REQUEST_CONSTANT1:

                try {
                    constant1 = parseConstantArgument(textFieldArgument.getText());
                } catch(NumberFormatException e) {
                    moderationListener.log("Failure while formatting entered value to the short number");
                    return;
                }

                textFieldArgument.setVisible(false);
                outputVoltageRequestPane.add(progressBar, 1, 0, 2, 1);

                moderationListener.log("Processing is started for OUTPUT_VOLTAGE"
                        + " constant 1: " + constant1);

                moderationTask1.config(ModerationTask.OUTPUT_VOLTAGE, (char) constant1);
                executorService.submit(moderationTask1);

                break;

            case INPUT_VOLTAGE1:

                try {
                    voltage1 = parseVoltageArgument(textFieldArgument.getText());
                    moderationListener.log("Output voltage 1 has been saved: " + voltage1);
                } catch(NumberFormatException e) {
                    moderationListener.log("Failure while formatting entered value to the double number");
                    return;
                }

                step = REQUEST_CONSTANT2;

                labelOrder.setText("3");
                textFieldArgument.clear();
                textFieldArgument.setVisible(true);
                textFieldArgument.setText(properties.getProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_CONST2));
                buttonRequest.setText("Request");
                buttonRequest.setDisable(false);

                break;

            case REQUEST_CONSTANT2:

                try {
                    constant2 = parseConstantArgument(textFieldArgument.getText());
                } catch(NumberFormatException e) {
                    moderationListener.log("Failure while formatting entered value to the short number");
                    return;
                }

                textFieldArgument.setVisible(false);
                outputVoltageRequestPane.add(progressBar, 1, 0, 2, 1);

                moderationListener.log("Processing is started for OUTPUT_VOLTAGE"
                        + " constant 2: " + constant2);

                moderationTask2.config(ModerationTask.OUTPUT_VOLTAGE, (char) constant2);
                executorService.submit(moderationTask2);

                break;

            case INPUT_VOLTAGE2:

                try {
                    voltage2 = parseVoltageArgument(textFieldArgument.getText());
                    moderationListener.log("Output voltage 2 has been saved: " + voltage2);
                } catch(NumberFormatException e) {
                    moderationListener.log("Failure while formatting entered value to the double number");
                    return;
                }

                moderationListener.log("Calculating output voltage coefficients");
                calculate();

                properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF1, Double.toString(coefficient1));
                properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF2, Double.toString(coefficient2));
                if (!fileWorker.storeConfigProperties(properties)) {
                    moderationListener.log("Storing data into serveiler.properties file has been failed.");
                }

                moderationListener.refresh(rowIndex);

                break;

        }

    }

    /**
     * https://stackoverflow.com/questions/6599548/convert-int-to-unsigned-short-java
     * @param input
     * @return
     * @throws NumberFormatException
     */
    private int parseConstantArgument(String input) throws NumberFormatException {
        if (input != null && !input.isEmpty()) {
            return Integer.parseInt(input);
        } else throw new NumberFormatException();
    }

    /**
     * https://discuss.kotlinlang.org/t/byte-to-unsigned-short-conversion/639
     * @param byte1
     * @param byte2
     * @param bigEndian
     * @return
     */
    public static int bytesToUnsignedShort(byte byte1, byte byte2, boolean bigEndian) {
        if (bigEndian)
            return ( ( (byte1 & 0xFF) << 8) | (byte2 & 0xFF) );
        return ( ( (byte2 & 0xFF) << 8) | (byte1 & 0xFF) );
    }

    private double parseVoltageArgument(String input) throws NumberFormatException {
        if (!(input == null || input.length() == 0)) {
            return Double.parseDouble(input);
        } else throw new NumberFormatException();
    }

    protected void calculate() {

        double a = (constant2 - constant1) / (voltage2 - voltage1);

        moderationListener.log("a = (constant2 - constant1) / (voltage2 - voltage1)");
        moderationListener.log("a = " + a);

        double b = constant1 - a * voltage1;

        moderationListener.log("b = constant1 - a * voltage1");
        moderationListener.log("b = " + b);

        coefficient1 = a * voltage1 + b;

        moderationListener.log("coefficient1 = a * voltage1 + b");
        moderationListener.log("coefficient1 = " + coefficient1);

        coefficient2 = a * voltage2 + b;

        moderationListener.log("coefficient2 = a * voltage2 + b");
        moderationListener.log("coefficient2 = " + coefficient2);

    }

}