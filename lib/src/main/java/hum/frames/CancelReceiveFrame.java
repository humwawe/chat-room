package hum.frames;

import hum.core.IoArgs;

import java.io.IOException;

/**
 * @author hum
 */
public class CancelReceiveFrame extends AbsReceiveFrame {
    public CancelReceiveFrame(byte[] header) {
        super(header);
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return 0;
    }
}
