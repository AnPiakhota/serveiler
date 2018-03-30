package com.itsm.controller.data.dto;

/**
 * Created by anpiakhota on 21.12.16.
 */
public class Data {

    /**
     * Current version of the controller firmware
     */
    public byte firmwareVersion;
    public byte pingStatus;
    public byte confirmationStatus;

    /**
     * Period of time between checks in milliseconds
     */
    public short checkInterval;

   /**
     * Output voltage moderation parameter
     */
    public char outputVoltage;

    public byte currentGainFactor;
    public byte voltageGainFactor;

    /* Buffer Content */

    /**
     * Status in buffer content while reading.
     */
    public byte readStatus;

    /**
     * Temperature value 32767 indicates that measuring sensor is broken
     */
    public short readTemperature;

    /**
     * Array contains buffer blocks.
     * The array should be initialized as
     * int[][] array = new int[read number of blocks][2];
     * Note there are only two values for columns.
     */
    public int[][] blockArray;

}
