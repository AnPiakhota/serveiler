package com.itsm.controller.data.dto.request;

import com.itsm.controller.data.dto.Data;
import com.itsm.controller.data.packet.Command;
import com.itsm.controller.data.packet.Specification;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Created by anpiakhota on 22.10.16.
 */

public interface Request extends Specification {

    Command getCommand();
    byte[] toBytes();
    ByteBuffer toBuffer();
    String toRequestMonitorString(ByteBuffer requestBuffer, int depth);
    String getRequestMonitorString();
    void setData(Supplier<Data> supplier);
    void put(ByteBuffer requestBuffer);

}
