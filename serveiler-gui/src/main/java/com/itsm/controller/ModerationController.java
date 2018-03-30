package com.itsm.controller;

import com.itsm.component.moderation.ModerationListener;
import com.itsm.component.moderation.request.CalibrationRequest;
import com.itsm.component.moderation.request.GainFactorRequest;
import com.itsm.component.moderation.request.OutputVoltageRequest;
import com.itsm.worker.FileWorker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ModerationController implements Initializable, ModerationListener {

    @FXML private TextField currentCoef1, currentCoef2;
    @FXML private TextField voltageCoef1, voltageCoef2;
    @FXML private TextField temperatureCoef1, temperatureCoef2;
    @FXML private TextField outputVoltageParam1, outputVoltageParam2;
    @FXML private TextField gainFactorCurrent, gainFactorVoltage;

    @FXML private Button windowCloseButton;
    @FXML private TextArea logTextArea;

    /* Calibration */
    @FXML private GridPane moderationGridPane;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FileWorker fileWorker;

    private Properties properties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        properties = fileWorker.getConfigProperties();

        windowCloseButton.setOnAction((event) -> ((Stage) windowCloseButton.getScene().getWindow()).close());

        updateData();
    }

    public void updateData() {

        currentCoef1.getStyleClass().remove("moderation-coefficient-unset-style");
        currentCoef1.getStyleClass().remove("moderation-coefficient-set-style");
        currentCoef2.getStyleClass().remove("moderation-coefficient-unset-style");
        currentCoef2.getStyleClass().remove("moderation-coefficient-set-style");
        voltageCoef1.getStyleClass().remove("moderation-coefficient-unset-style");
        voltageCoef1.getStyleClass().remove("moderation-coefficient-set-style");
        voltageCoef2.getStyleClass().remove("moderation-coefficient-unset-style");
        voltageCoef2.getStyleClass().remove("moderation-coefficient-set-style");
        temperatureCoef1.getStyleClass().remove("moderation-coefficient-unset-style");
        temperatureCoef1.getStyleClass().remove("moderation-coefficient-set-style");
        temperatureCoef2.getStyleClass().remove("moderation-coefficient-unset-style");
        temperatureCoef2.getStyleClass().remove("moderation-coefficient-set-style");
        outputVoltageParam1.getStyleClass().remove("moderation-coefficient-unset-style");
        outputVoltageParam1.getStyleClass().remove("moderation-coefficient-set-style");
        outputVoltageParam2.getStyleClass().remove("moderation-coefficient-unset-style");
        outputVoltageParam2.getStyleClass().remove("moderation-coefficient-set-style");
        gainFactorCurrent.getStyleClass().remove("moderation-coefficient-unset-style");
        gainFactorCurrent.getStyleClass().remove("moderation-coefficient-set-style");
        gainFactorVoltage.getStyleClass().remove("moderation-coefficient-unset-style");
        gainFactorVoltage.getStyleClass().remove("moderation-coefficient-set-style");

        /* Current */

        String currentPropCoef1 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF1);
        if (currentPropCoef1 == null || currentPropCoef1.equals("")) {
            currentCoef1.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            currentCoef1.getStyleClass().add("moderation-coefficient-set-style");
            currentCoef1.setText(currentPropCoef1);
        }

        String currentPropCoef2 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF2);
        if (currentPropCoef2 == null || currentPropCoef2.equals("")) {
            currentCoef2.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            currentCoef2.getStyleClass().add("moderation-coefficient-set-style");
            currentCoef2.setText(currentPropCoef2);
        }

        /* Voltage */

        String voltagePorpCoef1 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF1);
        if (voltagePorpCoef1 == null || voltagePorpCoef1.equals("")) {
            voltageCoef1.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            voltageCoef1.getStyleClass().add("moderation-coefficient-set-style");
            voltageCoef1.setText(voltagePorpCoef1);
        }

        String voltagePorpCoef2 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF2);
        if (voltagePorpCoef2 == null || voltagePorpCoef2.equals("")) {
            voltageCoef2.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            voltageCoef2.getStyleClass().add("moderation-coefficient-set-style");
            voltageCoef2.setText(voltagePorpCoef2);
        }

        /* Temperature */

        String temperaturePropCoef1 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF1);
        if (temperaturePropCoef1 == null || temperaturePropCoef1.equals("")) {
            temperatureCoef1.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            temperatureCoef1.getStyleClass().add("moderation-coefficient-set-style");
            temperatureCoef1.setText(temperaturePropCoef1);
        }

        String temperaturePropCoef2 = properties.getProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF2);
        if (temperaturePropCoef2 == null || temperaturePropCoef2.equals("")) {
            temperatureCoef2.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            temperatureCoef2.getStyleClass().add("moderation-coefficient-set-style");
            temperatureCoef2.setText(temperaturePropCoef2);
        }

        /* Output Voltage */

        String outputVoltagePropParam1 = properties.getProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF1);
        if (outputVoltagePropParam1 == null || outputVoltagePropParam1.equals("")) {
            outputVoltageParam1.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            outputVoltageParam1.getStyleClass().add("moderation-coefficient-set-style");
            outputVoltageParam1.setText(outputVoltagePropParam1);
        }

        String outputVoltagePropParam2 = properties.getProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF2);
        if (outputVoltagePropParam2 == null || outputVoltagePropParam2.equals("")) {
            outputVoltageParam2.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            outputVoltageParam2.getStyleClass().add("moderation-coefficient-set-style");
            outputVoltageParam2.setText(outputVoltagePropParam2);
        }

        /* Gain Factor */

        String gainFactorPropParam1 = properties.getProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_CURRENT);
        if (gainFactorPropParam1 == null || gainFactorPropParam1.equals("")) {
            gainFactorCurrent.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            gainFactorCurrent.getStyleClass().add("moderation-coefficient-set-style");
            gainFactorCurrent.setText(gainFactorPropParam1);
        }

        String gainFactorPropParam2 = properties.getProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_VOLTAGE);
        if (gainFactorPropParam2 == null || gainFactorPropParam2.equals("")) {
            gainFactorVoltage.getStyleClass().add("moderation-coefficient-unset-style");
        } else {
            gainFactorVoltage.getStyleClass().add("moderation-coefficient-set-style");
            gainFactorVoltage.setText(gainFactorPropParam2);
        }

    }

    @FXML
    private void refreshModeration(ActionEvent event) {

        currentCoef1.clear();
        currentCoef2.clear();
        voltageCoef1.clear();
        voltageCoef2.clear();
        temperatureCoef1.clear();
        temperatureCoef2.clear();
        outputVoltageParam1.clear();
        outputVoltageParam2.clear();
        gainFactorCurrent.clear();
        gainFactorVoltage.clear();

        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF1, "");
        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_CURRENT_COEF2, "");
        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF1, "");
        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_VOLTAGE_COEF2, "");
        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF1, "");
        properties.setProperty(fileWorker.PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF2, "");

        properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_CONST1, "1000");
        properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_CONST2, "40000");
        properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF1, "");
        properties.setProperty(fileWorker.PROP_MODERATION_OUTPUT_VOLTAGE_COEF2, "");

        properties.setProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_CURRENT, "");
        properties.setProperty(fileWorker.PROP_MODERATION_GAIN_FACTOR_VOLTAGE, "");

        fileWorker.storeConfigProperties(properties);

        if (logTextArea.isVisible()) {
            logTextArea.clear();
            logTextArea.setVisible(false);
        }

        updateData();

    }

    @FXML
    private void requestModeration(ActionEvent event) {

        if (logTextArea.isVisible()) {
            logTextArea.clear();
            logTextArea.setVisible(false);
        }

        int rowIndex = GridPane.getRowIndex(((Node)event.getSource()));

        moderationGridPane.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == rowIndex)
                .forEach(node -> node.setVisible(false));

        switch (rowIndex) {
            case 1:
                CalibrationRequest currentCalibrationRequest = applicationContext.getBean(CalibrationRequest.class);
                currentCalibrationRequest
                        .initialize(this, rowIndex)
                        .configTask(CalibrationRequest.CURRENT);
                moderationGridPane.add(currentCalibrationRequest, 0, rowIndex, 4, 1);
                break;
            case 2:
                CalibrationRequest voltageCalibrationRequest = applicationContext.getBean(CalibrationRequest.class);
                voltageCalibrationRequest
                        .initialize(this, rowIndex)
                        .configTask(CalibrationRequest.VOLTAGE);
                moderationGridPane.add(voltageCalibrationRequest, 0, rowIndex, 4, 1);
                break;
            case 3:
                CalibrationRequest temperatureCalibrationRequest = applicationContext.getBean(CalibrationRequest.class);
                temperatureCalibrationRequest
                        .initialize(this, rowIndex)
                        .configTask(CalibrationRequest.TEMPERATURE);
                moderationGridPane.add(temperatureCalibrationRequest, 0, rowIndex, 4, 1);
                break;
            case 6:
                OutputVoltageRequest outputVoltageRequest = applicationContext.getBean(OutputVoltageRequest.class);
                outputVoltageRequest
                        .initialize(this, rowIndex)
                        .configTask();
                moderationGridPane.add(outputVoltageRequest, 0, rowIndex, 4, 1);
                break;
            case 9:
                GainFactorRequest gainFactorRequest = applicationContext.getBean(GainFactorRequest.class);
                gainFactorRequest
                        .initialize(this, rowIndex, gainFactorCurrent.getText(), gainFactorVoltage.getText())
                        .configTask();
                moderationGridPane.add(gainFactorRequest, 0, rowIndex, 4, 1);
                break;
        }

    }

    @Override
    public void refresh(int rowIndex) {

        ObservableList<Node> rowChildren = moderationGridPane.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == rowIndex && n.isVisible())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        moderationGridPane.getChildren().removeAll(rowChildren);

        moderationGridPane.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == rowIndex)
                .forEach(node -> node.setVisible(true));

        updateData();

    }

    @Override
    public void log(String log) {

        if (!logTextArea.isVisible()) logTextArea.setVisible(true);

        String content = logTextArea.getText() + System.lineSeparator();
        logTextArea.setText(content += log);

    }

}
