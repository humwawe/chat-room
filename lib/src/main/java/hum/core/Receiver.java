package hum.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public interface Receiver extends Closeable {
    void setReceiveProcessor(IoArgs.IoArgsEventProcessor processor);

    boolean postReceiveAsync() throws IOException;
}
