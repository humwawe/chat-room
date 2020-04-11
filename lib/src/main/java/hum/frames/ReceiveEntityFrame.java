package hum.frames;

import hum.core.IoArgs;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * @author hum
 */
public class ReceiveEntityFrame extends AbsReceiveFrame {
    private WritableByteChannel channel;

    public ReceiveEntityFrame(byte[] header) {
        super(header);
    }

    public void bindPacketChannel(WritableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return channel == null ? args.setEmpty(bodyRemaining) : args.writeTo(channel);
    }
}
