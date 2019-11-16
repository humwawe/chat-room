package hum.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * @author hum
 */
public interface IoProvider extends Closeable {
    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    /**
     * when channel readFrom ready, write to buffer (ioArgs)
     */
    abstract class HandleInputCallback implements Runnable {
        @Override
        public final void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable {

        @Override
        public final void run() {
            canProviderOutput();
        }

        protected abstract void canProviderOutput();
    }

}
