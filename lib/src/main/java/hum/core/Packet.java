package hum.core;

import java.io.Closeable;

/**
 * @author hum
 */
public abstract class Packet implements Closeable {
    protected byte type;
    protected int length;

    public byte type() {
        return type;
    }

    public int length() {
        return length;
    }

}
