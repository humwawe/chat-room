package hum.core;

import java.io.Closeable;

/**
 * @author hum
 */
public interface ReceiveDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback {
        ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length);

        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
