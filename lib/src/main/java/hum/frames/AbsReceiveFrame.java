package hum.frames;

import hum.core.Frame;
import hum.core.IoArgs;

import java.io.IOException;

/**
 * @author hum
 */
public abstract class AbsReceiveFrame extends Frame {
    volatile int bodyRemaining;

    AbsReceiveFrame(byte[] header) {
        super(header);
        bodyRemaining = getBodyLength();
    }

    @Override
    public boolean handle(IoArgs args) throws IOException {
        if (bodyRemaining == 0) {
            return true;
        }
        bodyRemaining -= consumeBody(args);
        return bodyRemaining == 0;
    }

    protected abstract int consumeBody(IoArgs args) throws IOException;

    @Override
    public int getConsumableLength() {
        return bodyRemaining;
    }

    @Override
    public Frame nextFrame() {
        return null;
    }
}
