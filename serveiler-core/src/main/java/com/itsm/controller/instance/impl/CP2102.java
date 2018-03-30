package com.itsm.controller.instance.impl;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.InteractionFactoryProducer;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.PacketParserException;
import com.itsm.controller.instance.BaseController;
import com.itsm.controller.instance.IController;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Class manages connection and data providing.
 */
public class CP2102<T extends ComAPI> extends BaseController<T> implements IController<T>  {

    private AbstractInteractionFactory responseFactory = InteractionFactoryProducer.getFactory("RESPONSE");

    ByteBuffer requestDirectBuffer;
    ByteBuffer responseDirectBuffer;
    {
        requestDirectBuffer = ByteBuffer.allocateDirect(6); // 6 is the max possible number of bytes in whatever request
        requestDirectBuffer.order(ByteOrder.LITTLE_ENDIAN);

        responseDirectBuffer = ByteBuffer.allocateDirect(2048);
        responseDirectBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public CP2102(T api) throws ControllerCommunicationException {
        super.api = api;
        api.openPort();
    }

    /**
     * https://github.com/RishiGupta12/SerialPundit/blob/master/applications/Read-method-designs.md
     * @param request
     * @return
     * @throws ControllerCommunicationException
     * @throws PacketParserException
     */
    public synchronized Response request(Request request) throws ControllerCommunicationException, PacketParserException {

        /* Request */

        requestDirectBuffer.clear();
        request.put(requestDirectBuffer);
//        System.out.println(request.toRequestMonitorString(requestDirectBuffer, 2));

        /**
         * A delay is required between two requests
         * because sometimes execution freezes while
         * reading after a few instant repeated requests.
         */
        api.writeBytes(request.toBytes());

        /* Response */

        responseDirectBuffer.clear();
        int bytes, length = 0;

        do {

            responseDirectBuffer.put(api.readBytes());
            bytes = responseDirectBuffer.position();

            if (bytes == 0) {
                throw new ControllerCommunicationException("Controller response timeout exception");
            }

            if (bytes >= 4) {
                length = responseDirectBuffer.getShort(2);

                /* Debug

                switch (responseDirectBuffer.get(1)) {
                    case 'P':
                        System.out.println("RESPONSE COMMAND: PING");
                        break;
                    case 'S':
                        System.out.println("RESPONSE COMMAND: START");
                        break;
                    case 'X':
                        System.out.println("RESPONSE COMMAND: STOP");
                        break;
                    case 'V':
                        System.out.println("RESPONSE COMMAND: VERSION");
                        break;
                    case 'D':
                        System.out.println("RESPONSE COMMAND: OUTPUT_VOLTAGE");
                        break;
                    case 'G':
                        System.out.println("RESPONSE COMMAND: GAIN_FACTOR");
                        break;
                    case 'B':
                        System.out.println("RESPONSE COMMAND: BUFFER");
                        break;
                    case 'L':
                        System.out.println("RESPONSE COMMAND: " + responseDirectBuffer.get(1));
                        break;
                    case 'R':
                        System.out.println("RESPONSE COMMAND: " + responseDirectBuffer.get(1));
                        break;
                    default:
                        System.out.println("RESPONSE COMMAND: ERROR");

                }

                */

            }

        } while (bytes - 4 != length);

        responseDirectBuffer.flip(); // To read
        Response response = responseFactory.getResponse(request);
        response.parse(responseDirectBuffer);
//        System.out.println(response.toResponseMonitorString(responseDirectBuffer, 2));

        return response;
    }

    @Override
    public String[] listAvailablePorts() throws ControllerCommunicationException {
        return api.listAvailablePorts();
    }

//    /**
//     * https://github.com/RishiGupta12/SerialPundit/blob/master/applications/Read-method-designs.md
//     * @param request
//     * @return
//     * @throws ControllerCommunicationException
//     * @throws PacketParserException
//     */
//    public synchronized Response request(Request request) throws ControllerCommunicationException, PacketParserException {
//
//        /* Request */
//
//        requestDirectBuffer.clear();
//        request.put(requestDirectBuffer);
//        api.writeBytesDirect(requestDirectBuffer, 0, requestDirectBuffer.position());
//        System.out.println(request.toRequestMonitorString(requestDirectBuffer, 2));
//
//        /* Response */
//
//        responseDirectBuffer.clear();
//        int bytes, offset = 0, length = 4;
//
//        do {
//
//            bytes = api.readBytesDirect(responseDirectBuffer, offset, length);
//
//            if (bytes == 0) {
//                throw new ControllerCommunicationException("Controller response timeout exception");
//            }
//
//            offset += bytes;
//            length -= bytes;
//
//            if (offset == 4) {
//                length = responseDirectBuffer.getShort(2);
//
//                /* Debug
//
//                switch (responseDirectBuffer.get(1)) {
//                    case 'P':
//                        System.out.println("RESPONSE COMMAND: PING");
//                        break;
//                    case 'S':
//                        System.out.println("RESPONSE COMMAND: START");
//                        break;
//                    case 'X':
//                        System.out.println("RESPONSE COMMAND: STOP");
//                        break;
//                    case 'V':
//                        System.out.println("RESPONSE COMMAND: VERSION");
//                        break;
//                    case 'D':
//                        System.out.println("RESPONSE COMMAND: OUTPUT_VOLTAGE");
//                        break;
//                    case 'G':
//                        System.out.println("RESPONSE COMMAND: GAIN_FACTOR");
//                        break;
//                    case 'B':
//                        System.out.println("RESPONSE COMMAND: BUFFER");
//                        break;
//                    case 'L':
//                        System.out.println("RESPONSE COMMAND: " + responseDirectBuffer.get(1));
//                        break;
//                    case 'R':
//                        System.out.println("RESPONSE COMMAND: " + responseDirectBuffer.get(1));
//                        break;
//                    default:
//                        System.out.println("RESPONSE COMMAND: ERROR");
//
//                }
//
//                */
//
//            }
//
//        } while (length != 0);
//
//        Response response = responseFactory.getResponse(request);
//        response.parse(responseDirectBuffer);
//        System.out.println(response.toResponseMonitorString(responseDirectBuffer, 2));
//
//        return response;
//    }

}
