package hum.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public interface Sender extends Closeable {
    void setSendProcessor(IoArgs.IoArgsEventProcessor processor);

    boolean postSendAsync() throws IOException;
}
