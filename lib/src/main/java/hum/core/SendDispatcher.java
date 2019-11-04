package hum.core;

import java.io.Closeable;

/**
 * @author hum
 */
public interface SendDispatcher extends Closeable {
    void send(SendPacket packet);

    void cancel(SendPacket packet);
}
