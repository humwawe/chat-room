package hum.frames;

import hum.core.Frame;
import hum.core.IoArgs;
import hum.core.SendPacket;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * @author hum
 */
public class SendEntityFrame extends AbsSendPacketFrame {
    private final ReadableByteChannel channel;
    private final long unConsumeEntityLength;

    SendEntityFrame(short identifier, long entityLength, ReadableByteChannel channel, SendPacket packet) {
        super((int) Math.min(entityLength, Frame.MAX_CAPACITY), Frame.TYPE_PACKET_ENTITY, Frame.FLAG_NONE, identifier, packet);
        this.unConsumeEntityLength = entityLength - bodyRemaining;
        this.channel = channel;
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        if (packet == null) {
            return args.fillEmpty(bodyRemaining);
        }
        return args.readFrom(channel);
    }

    @Override
    public Frame buildNextFrame() {
        if (unConsumeEntityLength == 0) {
            return null;
        }
        return new SendEntityFrame(getBodyIdentifier(), unConsumeEntityLength, channel, packet);
    }
}
