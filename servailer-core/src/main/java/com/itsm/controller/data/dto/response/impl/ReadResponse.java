package com.itsm.controller.data.dto.response.impl;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.AbstractResponse;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.PacketParserException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by anpiakhota on 20.10.16.
 */
public class ReadResponse extends AbstractResponse {

    public ReadResponse(Request request) {
        super(request);
    }


    @Override
    public void parse(ByteBuffer responseBuffer) throws PacketParserException {

        if (responseBuffer == null) return;

        super.direction = responseBuffer.get();
        super.token = responseBuffer.get();

        /*
            Sent value is the total amount of bytes carrying data.
            To get the amount of check blocks we need to extract 5 bytes
            (status, temperature, current gain factor, voltage gain factor) first
            and divide by 6 which is the number of bytes for a check block.
         */
        super.length = (short) ((responseBuffer.getShort() - 5) / 6);

        super.data = new Data();

        super.data.readStatus = responseBuffer.get();
        super.data.readTemperature = responseBuffer.getShort();
        super.data.currentGainFactor = responseBuffer.get();
        super.data.voltageGainFactor = responseBuffer.get();

        /*
            To calculate block amount in buffer increment by 1 is needed
            because current position points to the next item index.
            That is if limit 6 and current position is 3, the result is 3
            but amount of unread data is 4.
         */
        int blockAmount = ((responseBuffer.limit() - responseBuffer.position()) + 1) / 6;

        // TODO debug blockAmount == super.length

        super.data.blockArray = new int[super.length][2];

        for (int i = 0, j = 0; i < super.length; ) {

            byte b1 = responseBuffer.get();
            byte b2 = responseBuffer.get();
            byte b3 = responseBuffer.get();

            super.data.blockArray[i][j] = (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16);

            switch (j) {
                case 0: j++; continue;
                case 1:
                    j = 0; i++;
                    /* Check if remaining is less than block size */
                    if (responseBuffer.remaining() < 6) return;
                    continue;
            }

        }

    }

}
