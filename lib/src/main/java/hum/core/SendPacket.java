package hum.core;

/**
 * @author hum
 */
public abstract class SendPacket extends Packet {
    private boolean isCanceled;

    public abstract byte[] bytes();

    public boolean isCanceled() {
        return isCanceled;
    }
}
