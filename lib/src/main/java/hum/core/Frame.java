package hum.core;

import java.io.IOException;

/**
 * length 16 0~65535
 * type 8 -127~128
 * flags 8 00000000
 * identifier 8 1~255
 * other 8 00000000
 * payload
 *
 * @author hum
 */
public abstract class Frame {
    // all-> 6 Byte
    public static final int FRAME_HEADER_LENGTH = 6;
    // 2^162 Byte -> 16 bits
    public static final int MAX_CAPACITY = 64 * 1024 - 1;

    public static final byte TYPE_PACKET_HEADER = 11;
    public static final byte TYPE_PACKET_ENTITY = 12;
    public static final byte TYPE_COMMAND_SEND_CANCEL = 41;
    public static final byte TYPE_COMMAND_RECEIVE_REJECT = 42;

    public static final byte FLAG_NONE = 0;

    protected final byte[] header = new byte[FRAME_HEADER_LENGTH];

    public Frame(int length, byte type, byte flag, short identifier) {
        if (length < 0 || length > MAX_CAPACITY) {
            throw new RuntimeException("The Body length of a single frame should be between 0 and " + MAX_CAPACITY);
        }
        if (identifier < 1 || identifier > 255) {
            throw new RuntimeException("The Body identifier of a single frame should be between 1 and 255");
        }
        // 00000000 00000000 00000000 01000000 int, use last 2 bytes
        header[0] = (byte) (length >> 8);
        header[1] = (byte) (length);

        header[2] = type;
        header[3] = flag;

        header[4] = (byte) identifier;
        header[5] = 0;
    }

    public Frame(byte[] header) {
        System.arraycopy(header, 0, this.header, 0, FRAME_HEADER_LENGTH);
    }

    public abstract boolean handle(IoArgs args) throws IOException;

    public abstract Frame nextFrame();

    public int getBodyLength() {
        return ((((int) header[0]) & 0xFF) << 8) | (((int) header[1]) & 0xFF);
    }

    public byte getBodyType() {
        return header[2];
    }

    public byte getBodyFlag() {
        return header[3];
    }

    public short getBodyIdentifier() {
        return (short) (((short) header[4]) & 0xFF);
    }
}
