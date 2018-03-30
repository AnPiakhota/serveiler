package com.itsm.worker;

import com.itsm.controller.ControllerFacade;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.packet.Specification;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by anpiakhota on 26.12.16.
 */
public class ModerationTask extends Task<Double> {

    private int type;

    private char outputVoltage;
    private short checkInterval;

    private byte currentGainFactor;
    private byte voltageGainFactor;

    public static final int CURRENT = 1;
    public static final int VOLTAGE = 2;
    public static final int TEMPERATURE = 3;
    public static final int OUTPUT_VOLTAGE = 4;
    public static final int GAIN_FACTOR = 5;

    @Autowired
    private ControllerFacade controllerFacade;

    @Autowired
    private AbstractInteractionFactory requestFactory;

    @Autowired
    private FileWorker fileWorker;

    /**
     * Configuration method to set OUTPUT VOLTAGE.
     * @param type
     * @param data
     */
    public void config(int type, char data) {
        this.type = type;
        this.outputVoltage = data;
    }

    /**
     * Configuration method to conduct CALIBRATION.
     * @param type
     * @param data
     */
    public void config(int type, short data) {
        this.type = type;
        this.checkInterval = data;
    }

    /**
     * Configuration method to set gain factor.
     * @param type
     * @param currentGainFactor
     * @param voltageGainFactor
     */
    public void config(int type, byte currentGainFactor, byte voltageGainFactor) {
        this.type = type;
        this.currentGainFactor = currentGainFactor;
        this.voltageGainFactor = voltageGainFactor;
    }

    @Override
    protected Double call() throws Exception {

        switch (type) {

            case CURRENT:
            case VOLTAGE:
            case TEMPERATURE:

                /* START */

                fileWorker.reset();

                Request startRequest = requestFactory.getRequest(Command.START);
                startRequest.setData(() -> {
                    Data data = new Data();
                    data.checkInterval = this.checkInterval;
                    return data;
                });

                Response startResponse = controllerFacade.action(startRequest).blockingGet();
                if (startResponse.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_SUCCESS) {

                    /* PING */

                    TimeUnit.MILLISECONDS.sleep((256 * checkInterval) + (checkInterval * 10 /* delay */));

                    Request pingRequest = requestFactory.getRequest(Command.PING);
                    Response pingResponse = controllerFacade.inquire(pingRequest).blockingGet();

                    if (pingResponse.getData().pingStatus == Specification.PING_STATUS_ON) { // Failure
                        throw new Exception("Ping status: ON");
                    }

                    /* STOP */

                    Request stopRequest = requestFactory.getRequest(Command.STOP);
                    Response stopResponse = controllerFacade.action(stopRequest).blockingGet();

                    int[] ints = null;

                    switch (type) {
                        case CURRENT:
                            ints = IntStream.range(0, stopResponse.getData().blockArray.length - 1)
                                    .map(i -> stopResponse.getData().blockArray[i][0]).toArray();

                            fileWorker.fileBuffer("current calibration", checkInterval,
                                    stopResponse.getData().readTemperature, stopResponse.getData().blockArray);

                            break;
                        case VOLTAGE:
                            ints = IntStream.range(0, stopResponse.getData().blockArray.length - 1)
                                    .map(i -> stopResponse.getData().blockArray[i][1]).toArray();

                            fileWorker.fileBuffer("voltage calibration", checkInterval,
                                    stopResponse.getData().readTemperature, stopResponse.getData().blockArray);

                            break;
                        case TEMPERATURE:
                            ints = new int[] {stopResponse.getData().readTemperature};

                            fileWorker.fileBuffer("temperature calibration", checkInterval,
                                    stopResponse.getData().readTemperature, stopResponse.getData().blockArray);

                            break;
                    }

                    OptionalDouble optionalDouble = Arrays.stream(ints).average();
                    if (optionalDouble.isPresent()) {
                        return optionalDouble.getAsDouble();
                    } else {
                        throw new Exception("Average calculation is failed");
                    }

                } else {
                    throw new Exception("START > Confirmation status: FAILURE");
                }

            case OUTPUT_VOLTAGE:

                if (outputVoltage == 0) {
                    throw new Exception("outputVoltage is not set");
                }

                Request outputVoltageRequest = requestFactory.getRequest(Command.OUTPUT_VOLTAGE);
                outputVoltageRequest.setData(() -> {
                    Data data = new Data();
                    data.outputVoltage = this.outputVoltage;
                    return data;
                });

                Response outputVoltageResponse = controllerFacade.action(outputVoltageRequest).blockingGet();
                if (outputVoltageResponse.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_SUCCESS) {
                    return 15D; // CONFIRMATION_STATUS_SUCCESS
                }
                if (outputVoltageResponse.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_FAILURE) {
                    throw new Exception("OUTPUT VOLTAGE > Confirmation status: FAILURE");
                }

                break;

            case GAIN_FACTOR:

                if (currentGainFactor == 0 || voltageGainFactor == 0) {
                    throw new Exception("Either currentGainFactor or voltageGainFactor is not set");
                }

                Request gainFactorRequest = requestFactory.getRequest(Command.GAIN_FACTOR);
                gainFactorRequest.setData(() -> {
                    Data data = new Data();
                    data.currentGainFactor = this.currentGainFactor;
                    data.voltageGainFactor = this.voltageGainFactor;
                    return data;
                });

                Response gainFactorResponse = controllerFacade.action(gainFactorRequest).blockingGet();
                if (gainFactorResponse.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_SUCCESS) {
                    return 15D; // CONFIRMATION_STATUS_SUCCESS
                }
                if (gainFactorResponse.getData().confirmationStatus == Specification.CONFIRMATION_STATUS_FAILURE) {
                    throw new Exception("GAIN FACTOR > Confirmation status: FAILURE");
                }

                break;

        }

        throw new Exception("Moderation task is failed");

    }

}
