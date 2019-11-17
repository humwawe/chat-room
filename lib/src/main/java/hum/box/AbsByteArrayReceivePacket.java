package hum.box;

import hum.core.ReceivePacket;

import java.io.ByteArrayOutputStream;

/**
 * @author hum
 */
public abstract class AbsByteArrayReceivePacket<Entity> extends ReceivePacket<ByteArrayOutputStream, Entity> {

    public AbsByteArrayReceivePacket(long len) {
        super(len);
    }

    @Override
    protected final ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
