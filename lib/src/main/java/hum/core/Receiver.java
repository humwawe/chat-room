package hum.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public interface Receiver extends Closeable {
    void setReceiveListener(IoArgs.IoArgsEventListener listener);

    /**
     * when buffer ready, callback listener
     *
     * @param args
     * @return boolean
     * @throws IOException
     */
    boolean receiveAsync(IoArgs args) throws IOException;
}
