package hum.core;


import java.io.IOException;
import java.io.OutputStream;

/**
 * @author hum
 */
public abstract class ReceivePacket<Stream extends OutputStream, Entity> extends Packet<Stream> {
    private Entity entity;

    public ReceivePacket(long len) {
        this.length = len;
    }

    public Entity entity() {
        return entity;
    }

    protected abstract Entity buildEntity(Stream stream);

    @Override
    protected final void closeStream(Stream stream) throws IOException {
        super.closeStream(stream);
        entity = buildEntity(stream);
    }
}
