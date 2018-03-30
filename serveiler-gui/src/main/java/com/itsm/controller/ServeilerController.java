package com.itsm.controller;

import com.itsm.config.SpringFxmlLoader;
import com.itsm.config.gui.StageConfiguration;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.packet.Specification;
import com.itsm.util.font.AwesomeFontIcon;
import com.itsm.worker.FileWorker;
import io.reactivex.Flowable;
import io.reactivex.subscribers.ResourceSubscriber;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by anpiakhota on 7.12.16.
 * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/line-chart.htm#CIHGBCFI
 * http://blog.buildpath.de/fxml-composition-how-to-get-the-controller-of-an-included-fxml-view-nested-controllers/
 *
 * http://www.guigarage.com/2014/05/datafx-tutorial-3/
 *
 */
public class ServeilerController implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private HBox portBar;

    @FXML
    private TextField textFieldComPort;

    @FXML
    private Spinner<Integer> spinnerChartDisplayRangeInput ;

    @FXML private Button buttonChartDisplayComposite, buttonChartDisplayVertical, buttonChartDisplayQuaternary;
    @FXML private Button buttonChartTickLess, buttonChartTickMore;
    @FXML private Button buttonStart, buttonPing, buttonCalibration;

    @FXML private Parent chart;
    // Note, that chartController not a partialy lowercased class name, but fx:id + Controller word.
    @FXML private LineChartController chartController;

    @FXML private Slider sliderScale;

    @FXML private Label statusMessageField;

    @FXML private ProgressBar statusProgressBar;

    @FXML private Label checkIntervalStatus, outputVoltageStatus;

    private final Tooltip tooltip = new Tooltip();

    @Autowired
    private StageConfiguration stageConfiguration;

    @Autowired
    private SpringFxmlLoader springFxmlLoader;

    @Autowired
    private ControllerFacade controllerFacade;

    @Autowired
    private AbstractInteractionFactory requestFactory;

    @Autowired
    private FileWorker fileWorker;

    private Properties properties;
    private char outputVoltage;
    private short checkInterval;
    private short readInterval;
    private int chartDisplayRange;

    private static final int CHART_DISPLAY_DEFAULT_RANGE = 5;


    private final int INPUT_DIALOG_INTERVAL = 1;
    private final int INPUT_DIALOG_PORT = 2;
    private final int INPUT_DIALOG_VOLTAGE = 3;

    /*
        Source: http://www.vipan.com/htdocs/bitwisehelp.html
    */
    private volatile int flags = 0;

    /**
     * The flag is set while running.
     */
    private final int TEST_RUNNING = 1 << 0; // 1

    private final int TEST_MODERATED = 1 << 1; // 2

    /**
     * This flag indicates setting check interval.
     */
    private final int TEST_RATED = 1 << 2; // 4

    private final int TEST_STOPPING = 1 << 3; // 8

    /**
     * The flag is set if application needs to be closed.
     */
    private final int APP_CLOSED = 1 << 4; // 16

    /**
     * The flag is set when output voltage is set.
     */
    private final int VOLTAGE_REQUESTED = 1 << 5; // 32

    /**
     * This method contains all logic regarding
     * UI state changes. Every action method should
     * call this method as last action.
     */
    public void control() {

        if ((flags & TEST_RUNNING) == TEST_RUNNING) {
            buttonStart.setText(AwesomeFontIcon.FA_STOP);
            buttonStart.setStyle("-fx-text-fill:#E53935;");
            statusProgressBar.setVisible(true);
        }

        if ((flags & TEST_STOPPING) == TEST_STOPPING) {
            buttonStart.setText(AwesomeFontIcon.FA_EXCLAMATION_TRIANGLE);
            buttonStart.setStyle("-fx-text-fill:#FB8C00;");
            statusProgressBar.setVisible(true);
        }

        if ((flags & TEST_RUNNING) != TEST_RUNNING && (flags & TEST_STOPPING) != TEST_STOPPING) {
            buttonStart.setText(AwesomeFontIcon.FA_PLAY);
            buttonStart.setStyle("-fx-text-fill:#8BC34A;");
            statusProgressBar.setVisible(false);
        }

        if ((flags & VOLTAGE_REQUESTED) == VOLTAGE_REQUESTED) {
            String status[] = outputVoltageStatus.getText().split(":");
            status[1] = String.valueOf((int) outputVoltage);
            outputVoltageStatus.setText(String.join(": ", status));
        }

        if ((flags & TEST_RATED) == TEST_RATED) {

            readInterval = (short) (checkInterval * 2); // TODO DEBUG

            String status[] = checkIntervalStatus.getText().split(":");
            status[1] = String.valueOf(checkInterval);
            checkIntervalStatus.setText(String.join(": ", status));
            checkIntervalStatus.setVisible(true);
        }

        if ((flags & APP_CLOSED) == APP_CLOSED && (flags & TEST_RUNNING) == TEST_RUNNING) {
            statusMessageField.setText("TEST_STOPPING");
            tooltip.setText("");
            flags |= TEST_STOPPING;
//            bufferReadSubscriber.dispose(); // Calling dispose cancels execution and last request can be unhandled.
            controllerFacade.stop(); // Run stop request in onComplete
        }

        if ((flags & APP_CLOSED) == APP_CLOSED && (flags & TEST_STOPPING) == TEST_STOPPING) {
            while ((flags & TEST_STOPPING) == TEST_STOPPING) Platform.exit();
        }

        if ((flags & APP_CLOSED) == APP_CLOSED && (flags & TEST_RUNNING) != TEST_RUNNING
                && (flags & TEST_STOPPING) != TEST_STOPPING) {
            Platform.exit();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {




//        communication(); TBD




        setupCustomTooltipBehavior(1, 10000, 1);

        Optional<ControllerCommunicationException> op = controllerFacade.isControllerInitialized();
        if (op.isPresent()) { // Exception while initialization
            textFieldComPort.getStyleClass().add("check-interval-unset-style");

            buttonStart.setDisable(true);
            buttonPing.setDisable(true);
            buttonCalibration.setDisable(true);

            StringWriter sw = new StringWriter();
            op.get().printStackTrace(new PrintWriter(sw));
            textFieldComPort.setTooltip(new Tooltip(sw.toString()));
        } else {
            textFieldComPort.getStyleClass().add("check-interval-set-style");
        }

        properties = fileWorker.getConfigProperties();

        if (fileWorker.isModerated()) flags |= TEST_MODERATED;

        if (fileWorker.isRated()) {
            try {
                checkInterval = Short.parseShort(properties.getProperty(fileWorker.PROP_SERVEILER_CHECK_INTERVAL));
                flags |= TEST_RATED;
            } catch (NumberFormatException e) { }
        }

        menuBar.setFocusTraversable(true);
        portBar.prefHeightProperty().bind(menuBar.heightProperty());
        textFieldComPort.setText(properties.getProperty(fileWorker.PROP_SERVEILER_COM_PORT));

        buttonChartTickLess.setOnMouseClicked((event) ->
                chartController.tickLess());
        buttonChartTickMore.setOnMouseClicked((event) ->
                chartController.tickMore());


        String sChartDisplayRange = properties.getProperty(fileWorker.PROP_TEST_CHART_DISPLAY_RANGE);
        if (!StringUtils.isEmpty(sChartDisplayRange)
                && sChartDisplayRange.chars().allMatch(Character::isDigit)) {
                chartDisplayRange = Integer.valueOf(sChartDisplayRange);
        } else {
            chartDisplayRange = CHART_DISPLAY_DEFAULT_RANGE;
        }
        spinnerChartDisplayRangeInput.getValueFactory().setValue(chartDisplayRange);

        SpinnerIncrementHandler handler = new SpinnerIncrementHandler();
        spinnerChartDisplayRangeInput.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);
        spinnerChartDisplayRangeInput.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
            Node node = evt.getPickResult().getIntersectedNode();
            if (node.getStyleClass().contains("increment-arrow-button") ||
                    node.getStyleClass().contains("decrement-arrow-button")) {
                if (evt.getButton() == MouseButton.PRIMARY) {
                    handler.stop();
                }
            }
        });

        SpinnerValueFactory<Integer> valueFactory = spinnerChartDisplayRangeInput.getValueFactory();
        TextFormatter spinnerChartDisplayRangeFormatter = new TextFormatter(valueFactory.getConverter(), valueFactory.getValue());
        spinnerChartDisplayRangeInput.getEditor().setTextFormatter(spinnerChartDisplayRangeFormatter);
        valueFactory.valueProperty().bindBidirectional(spinnerChartDisplayRangeFormatter.valueProperty());

        spinnerChartDisplayRangeInput.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue < CHART_DISPLAY_DEFAULT_RANGE) {
                chartDisplayRange = CHART_DISPLAY_DEFAULT_RANGE;
            } else {
                chartDisplayRange = newValue;
            }
        });

        buttonChartDisplayComposite.setOnMouseClicked((event) ->
                chartController.display(LineChartController.ChartLayout.COMPOSITE));
        buttonChartDisplayVertical.setOnMouseClicked((event) ->
                chartController.display(LineChartController.ChartLayout.VERTICAL));
        buttonChartDisplayQuaternary.setOnMouseClicked((event) ->
                chartController.display(LineChartController.ChartLayout.QUATERNARY));

        chartController.setSliders(sliderScale);

        statusMessageField.setTooltip(tooltip);

        statusProgressBar.prefHeightProperty().bind(checkIntervalStatus.heightProperty());

        stageConfiguration.getPrimaryStage().setOnCloseRequest(e -> {
            properties.setProperty(fileWorker.PROP_TEST_CHART_DISPLAY_RANGE, Integer.toString(chartDisplayRange));
            exit(null);
        });

        control();

    }

    @FXML
    public void refresh(ActionEvent event) {
        statusMessageField.setText("Refreshing application state");

        if ((flags & VOLTAGE_REQUESTED) == VOLTAGE_REQUESTED) {
            flags = 0;
            flags |= VOLTAGE_REQUESTED;
        } else {
            flags = 0;
        }

        properties = fileWorker.getConfigProperties();
        if (fileWorker.isModerated()) flags |= TEST_MODERATED;
        if (fileWorker.isRated()) {
            try {
                checkInterval = Short.parseShort(properties.getProperty(fileWorker.PROP_SERVEILER_CHECK_INTERVAL));
                flags |= TEST_RATED;
            } catch (NumberFormatException e) { }
        }
        chartController.refresh();
        statusMessageField.setText("Refreshed");
        control();
    }

    @FXML
    public void chooseFile(ActionEvent event) {

        if ((flags & TEST_RUNNING) == TEST_RUNNING || (flags & TEST_STOPPING) == TEST_STOPPING) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            DialogPane dialogPane = alert.getDialogPane();

            dialogPane.getStylesheets().add(
                    getClass().getClassLoader().getResource("dialog-theme.css").toExternalForm());
//            dialogPane.getStyleClass().add("myDialog");

            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("The test is running");
            alert.setContentText("Do you want to interrupt it?");
            alert.initOwner(stageConfiguration.getPrimaryStage());
            alert.initStyle(StageStyle.UNIFIED);
            alert.initModality(Modality.WINDOW_MODAL);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                statusMessageField.setText("INTERRUPTED");
                tooltip.setText("");

                if ((flags & TEST_RUNNING) == TEST_RUNNING) {
                    flags |= TEST_STOPPING;
                    controllerFacade.stop(); // Run stop request in onComplete
                }

            } else {
                statusMessageField.setText("TEST RUNNING");
                tooltip.setText("");
            }

            control();

        }

        if ((flags & TEST_RUNNING) != TEST_RUNNING && (flags & TEST_STOPPING) != TEST_STOPPING) {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select CSV test file");
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fileChooser.showOpenDialog(stageConfiguration.getPrimaryStage());

            if (file != null) {
                chartController.refresh();
                chartController.executeLoadTask(file.toPath());
            } else {
                statusMessageField.setText("No file was selected");
                tooltip.setText("");
            }

        }

    }

    @FXML
    public void ping(ActionEvent event) {

        Request pingRequest = requestFactory.getRequest(Command.PING);
        controllerFacade.inquire(pingRequest).subscribe(
                onSuccess -> {
                    Platform.runLater(() -> {

                       if (onSuccess.getData().pingStatus == Specification.PING_STATUS_ON) {
                            statusMessageField.setText("< Ping: SUCCESS");
                            tooltip.setText(onSuccess.getResponseMonitorString());
                            flags |= TEST_RUNNING;
                        }

                        if (onSuccess.getData().pingStatus == Specification.PING_STATUS_OFF) {
                            statusMessageField.setText("< Ping: FAILURE");
                            tooltip.setText(onSuccess.getResponseMonitorString());
                            flags &= ~TEST_RUNNING;
                        }

                        control();

                    });
                },
                onError -> {
                    Platform.runLater(() -> {
                        statusMessageField.setText("< Ping: FAILURE");

                        StringWriter sw = new StringWriter();
                        onError.printStackTrace(new PrintWriter(sw));
                        tooltip.setText(sw.toString());

                        control();

                    });
                });

    }

    @FXML
    public void moderate(ActionEvent event) {

        Parent calibration = null;
        try {
            calibration = (Parent) springFxmlLoader.load("moderation.fxml", ModerationController.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stage moderationStage = new Stage(StageStyle.TRANSPARENT);
        moderationStage.setResizable(true);
        moderationStage.initStyle(StageStyle.UTILITY);
        moderationStage.initOwner(stageConfiguration.getPrimaryStage());
        moderationStage.initModality(Modality.WINDOW_MODAL);
        moderationStage.setScene(new Scene(calibration, Color.RED));
        moderationStage.showAndWait();

    }

    @FXML
    public void communication() {

        Dialog dialog = null;
        try {
            dialog = (Dialog) springFxmlLoader.load("available-ports-dialog.fxml", this.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }


        DialogPane dialogPane = dialog.getDialogPane();
        TextArea input = (TextArea) dialogPane.lookup("#logTextArea");

        String[] ports = null;
        try {
            ports = controllerFacade.listAvailablePorts();
        } catch (ControllerCommunicationException e) {
            e.printStackTrace();
        }

        input.setText(ports.toString());

        Label header = (Label) dialogPane.lookup("#dialogHeader");

        ButtonType buttonType_APPLY = new ButtonType("Apply");
        dialog.getDialogPane().getButtonTypes().addAll(
                buttonType_APPLY, ButtonType.CANCEL
        );

        for (ButtonType btn : dialog.getDialogPane().getButtonTypes()) {
            Button button = (Button) dialog.getDialogPane().lookupButton(btn);
            button.setMaxWidth(Double.MAX_VALUE);
        }

        dialog.initOwner(stageConfiguration.getPrimaryStage());
        dialog.initModality(Modality.WINDOW_MODAL);

//        if (dialogType == INPUT_DIALOG_INTERVAL) {
//            header.setText("Current test will be stopped on applying changes");
//            input.setPromptText("Enter check interval");
//        }
//
//        if (dialogType == INPUT_DIALOG_PORT) {
//            header.setText("Application has to be restarted to pick up changes");
//            input.setPromptText("Enter com port");
//        }

//        Optional<ButtonType> result = dialog.showAndWait();
//        if (result.get() == buttonType_APPLY) {
//            return Optional.ofNullable(input.getText()).filter(s -> !s.isEmpty());
//        } else {
//            return Optional.ofNullable(null);
//        }

    }

    @FXML
    public void reloadFirmware(ActionEvent event) {
       statusMessageField.setText("TBD - Firmware reload TEST_RUNNING");
    }

    @FXML
    public void exit(ActionEvent event) {
        statusMessageField.setText("Application closing...");
        flags |= APP_CLOSED;
        control();
    }

    /**
     * The method starts and stops test.
     * If either moderation or check interval is not set
     * it opens corresponded control to handle these settings.
     * @param event
     */
    @FXML
    public void runTest(ActionEvent event) {

        if ((flags & TEST_RATED) != TEST_RATED) {
            storeCheckInterval(null);
            statusMessageField.setText("Check interval is not set");
            tooltip.setText("");
            return;
        }

        if ((flags & TEST_MODERATED) != TEST_MODERATED) {
            moderate(null);
            statusMessageField.setText("One or more parameters are not calibrated");
            tooltip.setText("");
            return;
        }

        if ((flags & TEST_RUNNING) == TEST_RUNNING) {
            statusMessageField.setText("TEST_STOPPING");
            tooltip.setText("");
            flags |= TEST_STOPPING;
//            bufferReadSubscriber.dispose(); // Calling dispose cancels execution and last request can be unhandled.
            controllerFacade.stop(); // Run stop request in onComplete
            control();
            return;
        }

        if ((flags & TEST_STOPPING) == TEST_STOPPING) {
            statusMessageField.setText("TEST_STOPPING");
            tooltip.setText("");
            return;
        }

        if ((flags & VOLTAGE_REQUESTED) != VOLTAGE_REQUESTED) {
            statusMessageField.setText("Request OUTPUT VOLTAGE for value: " + (int) outputVoltage);
            tooltip.setText("");
            outputVoltage(outputVoltage);
            return;
        }

        /* STARTING */

        fileWorker.reset();
        chartController.refresh();

        Request startRequest = requestFactory.getRequest(Command.START);
        startRequest.setData(() -> {
            Data data = new Data();
            data.checkInterval = this.checkInterval;
            return data;
        });

        controllerFacade.action(startRequest).subscribe(
                onSuccess -> {

                    Platform.runLater(() -> {

                        Data data = onSuccess.getData();

                        if (data.confirmationStatus == Specification.CONFIRMATION_STATUS_SUCCESS) {
                            statusMessageField.setText("< Start: SUCCESS");
                            tooltip.setText(onSuccess.getResponseMonitorString());
                            flags |= TEST_RUNNING;
                            control();

                            /* BUFFER */
                            Request bufferRequest = requestFactory.getRequest(Command.BUFFER);
                            Flowable<Response> floawbleReadObj = controllerFacade.read(bufferRequest, readInterval);
                            floawbleReadObj.subscribe(new ReadSubscriber());
                        }

                        if (data.confirmationStatus == Specification.CONFIRMATION_STATUS_FAILURE) {
                            statusMessageField.setText("< Start: FAILURE");
                            tooltip.setText(onSuccess.getResponseMonitorString());
                            flags &= ~TEST_RUNNING;
                            control();
                        }

                    });


                },

                onError -> {
                    Platform.runLater(() -> {
                        statusMessageField.setText("< Start: FAILURE");

                        StringWriter sw = new StringWriter();
                        onError.printStackTrace(new PrintWriter(sw));
                        tooltip.setText(sw.toString());
                    });
                });

    }

    public void outputVoltage(char outVolt) {

        Request outputVoltageRequest = requestFactory.getRequest(Command.OUTPUT_VOLTAGE);
        outputVoltageRequest.setData(() -> {
            Data data = new Data();
            data.outputVoltage = outVolt;
            return data;
        });

        controllerFacade.action(outputVoltageRequest).subscribe(
            onSuccess -> {
                Platform.runLater(() -> {

                    if (onSuccess.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_SUCCESS) {


                        statusMessageField.setText("< Output voltage: SUCCESS");
                        tooltip.setText(onSuccess.getResponseMonitorString());

                        statusMessageField.setText("Output voltage updated: " + (int) outVolt);

                        this.outputVoltage = outVolt;
                        flags |= VOLTAGE_REQUESTED;

                        control();

                    }

                    if (onSuccess.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_FAILURE) {
                        statusMessageField.setText("< Output voltage: FAILURE");
                        tooltip.setText(onSuccess.getResponseMonitorString());
                    }

                });
            },
            onError -> {
                Platform.runLater(() -> {
                    statusMessageField.setText("< Output voltage: FAILURE");

                    StringWriter sw = new StringWriter();
                    onError.printStackTrace(new PrintWriter(sw));
                    tooltip.setText(sw.toString());

                });
            });

    }


    @FXML
    public void storeCheckInterval(ActionEvent event) {

        Optional<String> input = showInputDialog(INPUT_DIALOG_INTERVAL, checkIntervalStatus.getText().split(":")[1].trim());

        if (input.isPresent()) {

            try {
                checkInterval = Short.parseShort(input.get());
                properties.setProperty(fileWorker.PROP_SERVEILER_CHECK_INTERVAL, String.valueOf(checkInterval));
                if (!fileWorker.storeConfigProperties(properties)) {
                    statusMessageField.setText("Storing properties failure");
                } else {
                    statusMessageField.setText("Properties updated (check interval): " + checkInterval);
                    flags |= TEST_RATED;
                }
            } catch (NullPointerException|NumberFormatException e) {

                statusMessageField.setText("Input parse error. See tooltip for more info");

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tooltip.setText(sw.toString());

            }

        } else {
            statusMessageField.setText("No changes for check interval");
        }

        control();

    }

    @FXML
    public void requestOutputVoltage(ActionEvent event) {

        Optional<String> input = showInputDialog(INPUT_DIALOG_VOLTAGE, outputVoltageStatus.getText().split(":")[1].trim());

        if (input.isPresent()) {

            try {

                outputVoltage((char) Integer.parseInt(input.get()));

            } catch (NullPointerException|NumberFormatException e) {

                statusMessageField.setText("Input parse error. See tooltip for more info");

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tooltip.setText(sw.toString());

            }

        } else {
            statusMessageField.setText("No changes for output voltage");
        }

    }

    @FXML
    public void setComPort(MouseEvent event) {

        Optional<String> input;
        if (event != null) {
            TextField output = (TextField) event.getSource();
            input = showInputDialog(INPUT_DIALOG_INTERVAL, output.getText());
        } else {
            input = showInputDialog(INPUT_DIALOG_INTERVAL, null);
        }

        if (input.isPresent()) {
            properties.setProperty(fileWorker.PROP_SERVEILER_COM_PORT, input.get());
            if (!fileWorker.storeConfigProperties(properties)) {
                statusMessageField.setText("Storing properties failure - " +
                        "used port: " + properties.getProperty(fileWorker.PROP_SERVEILER_COM_PORT));
            } else {
                flags |= APP_CLOSED;
            }
        } else {
            statusMessageField.setText("No changes - used port: " + properties.getProperty(fileWorker.PROP_SERVEILER_COM_PORT));
            tooltip.setText("");
        }

        control();

    }

    private Optional<String> showInputDialog(int dialogType, String text) {

/*

//            http://www.programcreek.com/java-api-examples/index.php?api=javafx.fxml.FXMLLoader
//            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("input-dialog.fxml"));
//            loader.setRoot(Stage.class.cast(Control.class.cast(event.getSource()).getScene().getWindow()));
//            loader.setController(this);
//            loader.setClassLoader(getClass().getClassLoader());


        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("input-dialog.fxml"));
//            loader.setRoot(Stage.class.cast(Control.class.cast(event.getSource()).getScene().getWindow()));
//            loader.setController(this);


//        ServeilerController mController = new ServeilerController();
//        loader.setController(mController);

        Dialog dialog = null;
        try {
            dialog = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ButtonType buttonType_APPLY = new ButtonType("Reset");
        dialog.getDialogPane().getButtonTypes().addAll(
                buttonType_APPLY, ButtonType.CANCEL
        );

        dialog.initOwner(Stage.class.cast(Control.class.cast(event.getSource()).getScene().getWindow()));
        dialog.initModality(Modality.WINDOW_MODAL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.get() == buttonType_APPLY){
            // ... user chose "One"
        } else {
            // ... user chose CANCEL or closed the dialog
        }
*/


        Dialog dialog = null;
        try {
            dialog = (Dialog) springFxmlLoader.load("input-dialog.fxml", this.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DialogPane dialogPane = dialog.getDialogPane();
        TextField input = (TextField) dialogPane.lookup("#dialogInput");
        input.setText(text);

        Label header = (Label) dialogPane.lookup("#dialogHeader");

        ButtonType buttonType_APPLY = new ButtonType("Apply");
        dialog.getDialogPane().getButtonTypes().addAll(
                buttonType_APPLY, ButtonType.CANCEL
        );

        for (ButtonType btn : dialog.getDialogPane().getButtonTypes()) {
            Button button = (Button) dialog.getDialogPane().lookupButton(btn);
            button.setMaxWidth(Double.MAX_VALUE);
        }

        dialog.initOwner(stageConfiguration.getPrimaryStage());
        dialog.initModality(Modality.WINDOW_MODAL);

        if (dialogType == INPUT_DIALOG_PORT) {
            header.setText("Application has to be restarted to pick up changes");
            input.setPromptText("Enter com port");
        }

        if (dialogType == INPUT_DIALOG_INTERVAL) {
            header.setText("Enter check interval");
            input.setPromptText("Enter check interval");
        }

        if (dialogType == INPUT_DIALOG_VOLTAGE) {
            header.setText("Enter output voltage");
            input.setPromptText("Enter output voltage");
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.get() == buttonType_APPLY) {
            return Optional.ofNullable(input.getText()).filter(s -> !s.isEmpty());
        } else {
            return Optional.ofNullable(null);
        }

    }

    private final class ReadSubscriber extends ResourceSubscriber<Response> {

        private List<String> lines = new ArrayList<>();
        private long currentCheckStamp;
        private int currentReadNumber;

        @Override
        public void onStart() {
            request(Long.MAX_VALUE);

            /* Deprecated because it draws all the data
            Platform.runLater(() -> {
                chartController.startLoadService();
                statusMessageField.setText("TEST_RUNNING");
                tooltip.setText("");
            });
            */

            Platform.runLater(() -> {
                statusMessageField.setText("TEST_RUNNING");
                tooltip.setText("");
            });

        }

        @Override
        public void onNext(Response response) {

            try {

                /* Deprecated because it draws all the data
                fileWorker.updateFile(checkInterval,
                        response.getData().readTemperature, response.getData().blockArray);

                Platform.runLater(() -> {
                    chartController.restartLoadService();
                });
                */


                Platform.runLater(() -> {
                    chartController.updateSeries(response, chartDisplayRange, checkInterval);
                });

                fileWorker.updateFile(++currentReadNumber, checkInterval,
                        response.getData().readTemperature, response.getData().blockArray);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onComplete() {

            flags &= ~TEST_RUNNING;

            Platform.runLater(() -> {
                statusMessageField.setText("TEST_STOPPING");
                tooltip.setText("");
            });

            Request stopRequest = requestFactory.getRequest(Command.STOP);
            controllerFacade.action(stopRequest).subscribe(
                    onSuccess -> {
                        Platform.runLater(() -> {

                            if (onSuccess.getData().readStatus == Specification.BUFFER_STATUS_OFF) {
                                statusMessageField.setText("< Stop: SUCCESS");
                                tooltip.setText(onSuccess.getResponseMonitorString());

                                try {

                                    /* Deprecated because it draws all the data
                                    fileWorker.updateFile(checkInterval,
                                            onSuccess.getData().readTemperature, onSuccess.getData().blockArray);
                                    chartController.stopLoadService();
                                    */

                                    Platform.runLater(() -> {
                                        chartController.updateSeries(onSuccess, chartDisplayRange, checkInterval);
                                    });

                                    fileWorker.updateFile(++currentReadNumber, checkInterval,
                                            onSuccess.getData().readTemperature, onSuccess.getData().blockArray);

                                } catch (Exception e) {
                                    statusMessageField.setText("Saving data to file was failed");

                                    /* Deprecated because it draws all the data
                                    chartController.stopLoadService();
                                    */

                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    tooltip.setText(sw.toString());
                                }

                            }

                            if (onSuccess.getData().readStatus == Specification.BUFFER_STATUS_ON) {

                                /* Deprecated because it draws all the data
                                chartController.stopLoadService();
                                */

                                statusMessageField.setText("< Stop: FAILURE");
                                tooltip.setText(onSuccess.getResponseMonitorString());
                            }

                            flags &= ~TEST_STOPPING;
                            control();

                        });

                    },
                    onError -> {
                        Platform.runLater(() -> {

                            /* Deprecated because it draws all the data
                            chartController.stopLoadService();
                            */

                            statusMessageField.setText("< Stop: FAILURE");
                            StringWriter sw = new StringWriter();
                            onError.printStackTrace(new PrintWriter(sw));
                            tooltip.setText(sw.toString());
                        });
                    });


        }

    }



    /* PING
    Request pingRequest = requestFactory.getRequest(Command.PING);
    controllerFacade.inquire(pingRequest).subscribe(
        onSuccess -> System.out.println(onSuccess.toString()),
        onError -> System.out.println(onError.getStackTrace()));
    */

    /* VERSION
    Request versionRequest = requestFactory.getRequest(Command.VERSION);
    controllerFacade.inquire(versionRequest).subscribe(
        onSuccess -> System.out.println(onSuccess.toString()),
        onError -> System.out.println(onError.getStackTrace()));
    */

    /* START
    Request startRequest = requestFactory.getRequest(Command.START);
            startRequest.setData(() -> {
        Data data = new Data();
        data.checkInterval = 500;
        return data;
    });

            controllerFacade.action(startRequest).subscribe(
            onSuccess -> System.out.println(onSuccess.toString()),
    onError -> System.out.println(onError.getStackTrace()));

    */

    /* OUTPUT VOLTAGE
    Request outputVoltageRequest = requestFactory.getRequest(Command.OUTPUT_VOLTAGE);
    startRequest.setData(() -> {
        Data data = new Data();
        data.checkInterval = 20;
        return data;
    });

    controllerFacade.action(outputVoltageRequest).subscribe(
        onSuccess -> System.out.println(onSuccess.toString()),
        onError -> System.out.println(onError.getStackTrace()));

    */

    /* STOP
    Request stopRequest = requestFactory.getRequest(Command.STOP);
    controllerFacade.action(stopRequest).subscribe(
            onSuccess -> System.out.println(onSuccess.toString()),
            onError -> System.out.println(onError.getStackTrace()));
    */


    /* BUFFER
    Request bufferRequest = requestFactory.getRequest(Command.BUFFER);
    Flowable<Response> floawbleReadObj = controllerFacade.read(bufferRequest, 500);
    floawbleReadObj.subscribe(subscriber);

    subscriber.dispose(); // Cancel subscription
    */

//    static ResourceSubscriber<Response> subscriber = new ResourceSubscriber<Response>() {
//        @Override
//        public void onStart() {
//            request(Long.MAX_VALUE);
//        }
//
//        @Override
//        public void onNext(Response t) {
//            System.out.println(t);
//        }
//
//        @Override
//        public void onError(Throwable t) {
//            t.printStackTrace();
//        }
//
//        @Override
//        public void onComplete() {
//            System.out.println("Done");
//        }
//    };

    /**
     *
     * Source: https://coderanch.com/t/622070/java/control-Tooltip-visible-time-duration
     *
     * <p>
     * Tooltip behavior is controlled by a private class javafx.scene.control.Tooltip$TooltipBehavior.
     * All Tooltips share the same TooltipBehavior instance via a static private member BEHAVIOR, which
     * has default values of 1sec for opening, 5secs visible, and 200 ms close delay (if mouse exits from node before 5secs).
     *
     * The hack below constructs a custom instance of TooltipBehavior and replaces private member BEHAVIOR with
     * this custom instance.
     * </p>
     *
     */
    private void setupCustomTooltipBehavior(int openDelayInMillis, int visibleDurationInMillis, int closeDelayInMillis) {
        try {

            Class TTBehaviourClass = null;
            Class<?>[] declaredClasses = Tooltip.class.getDeclaredClasses();
            for (Class c:declaredClasses) {
                if (c.getCanonicalName().equals("javafx.scene.control.Tooltip.TooltipBehavior")) {
                    TTBehaviourClass = c;
                    break;
                }
            }
            if (TTBehaviourClass == null) {
                // abort
                return;
            }
            Constructor constructor = TTBehaviourClass.getDeclaredConstructor(
                    Duration.class, Duration.class, Duration.class, boolean.class);
            if (constructor == null) {
                // abort
                return;
            }
            constructor.setAccessible(true);
            Object newTTBehaviour = constructor.newInstance(
                    new Duration(openDelayInMillis), new Duration(visibleDurationInMillis),
                    new Duration(closeDelayInMillis), false);
            if (newTTBehaviour == null) {
                // abort
                return;
            }
            Field ttbehaviourField = Tooltip.class.getDeclaredField("BEHAVIOR");
            if (ttbehaviourField == null) {
                // abort
                return;
            }
            ttbehaviourField.setAccessible(true);

            // Cache the default behavior if needed.
            Object defaultTTBehavior = ttbehaviourField.get(Tooltip.class);
            ttbehaviourField.set(Tooltip.class, newTTBehaviour);

        } catch (Exception e) {
            System.out.println("Aborted setup due to error:" + e.getMessage());
        }
    }


    private static final PseudoClass PRESSED = PseudoClass.getPseudoClass("pressed");

    /**
     * https://stackoverflow.com/questions/41050085/javafx-spinner-change-is-slow-with-click-and-hold-of-mouse-button
     */
    class SpinnerIncrementHandler implements EventHandler<MouseEvent> {

        private boolean increment;
        private long startTimestamp;

        private static final long DELAY = 1000l * 1000L * 750L; // 0.75 sec
        private Node button;

        private final AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now - startTimestamp >= DELAY) {
                    // trigger updates every frame once the initial delay is over
                    if (increment) {
                        spinnerChartDisplayRangeInput.increment();
                    } else {
                        spinnerChartDisplayRangeInput.decrement();
                    }
                }
            }
        };

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY) {
                Spinner source = (Spinner) event.getSource();
                Node node = event.getPickResult().getIntersectedNode();

                Boolean increment = null;
                // find which kind of button was pressed and if one was pressed
                while (increment == null && node != source) {
                    if (node.getStyleClass().contains("increment-arrow-button")) {
                        increment = Boolean.TRUE;
                    } else if (node.getStyleClass().contains("decrement-arrow-button")) {
                        increment = Boolean.FALSE;
                    } else {
                        node = node.getParent();
                    }
                }
                if (increment != null) {
                    event.consume();
                    source.requestFocus();
                    spinnerChartDisplayRangeInput = source;
                    this.increment = increment;

                    // timestamp to calculate the delay
                    startTimestamp = System.nanoTime();

                    button = node;

                    // update for css styling
                    node.pseudoClassStateChanged(PRESSED, true);

                    // first value update
                    timer.handle(startTimestamp + DELAY);

                    // trigger timer for more updates later
                    timer.start();
                }
            }
        }

        public void stop() {
            timer.stop();
            button.pseudoClassStateChanged(PRESSED, false);
            button = null;
            spinnerChartDisplayRangeInput = null;
        }
    }


}
