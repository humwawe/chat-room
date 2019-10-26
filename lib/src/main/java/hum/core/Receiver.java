package hum.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public interface Receiver extends Closeable {
    boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException;
}
