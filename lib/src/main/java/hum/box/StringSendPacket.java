package hum.box;

import hum.core.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static hum.core.Packet.TYPE_MEMORY_STRING;

/**
 * @author hum
 */
public class StringSendPacket extends BytesSendPacket {
    public StringSendPacket(String msg) {
        super(msg.getBytes());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
