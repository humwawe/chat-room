package hum.core;

/**
 * @author hum
 */
public abstract class ReceivePacket extends Packet {
    public abstract void save(byte[] bytes, int count);
}
