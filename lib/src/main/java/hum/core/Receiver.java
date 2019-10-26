package hum.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public interface Receiver extends Closeable {
    /**
     * when buffer ready, callback listener
     *
     * @param listener
     * @return boolean
     * @throws IOException
     */
    boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException;
}
