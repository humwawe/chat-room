package hum.box;

import hum.core.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author hum
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {
    private String string;

    public StringReceivePacket(int len) {
        length = len;
    }

    public String string() {
        return string;
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }
}
