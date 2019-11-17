package hum.impl.async;

import hum.box.StringReceivePacket;
import hum.core.*;
import hum.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hum
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcessor {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;
    private ReceivePacket<?, ?> packetTemp;
    private IoArgs ioArgs = new IoArgs();
    private long total;
    private long position;
    private WritableByteChannel packetChannel;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveProcessor(this);
        // when packet ready, callback
        this.callback = callback;
    }

    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLength();
            byte type = length > 200 ? Packet.TYPE_STREAM_FILE : Packet.TYPE_MEMORY_STRING;
            packetTemp = callback.onArrivedNewPacket(type, length);
            packetChannel = Channels.newChannel(packetTemp.open());
            total = length;
            position = 0;
        }
        try {
            int count = args.writeTo(packetChannel);
            position += count;
            if (position == total) {
                completePacket(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            completePacket(false);
        }

    }

    private void completePacket(boolean isSucceed) {
        ReceivePacket packet = this.packetTemp;
        // build entity when close
        CloseUtils.close(packet);
        packetTemp = null;
        WritableByteChannel channel = this.packetChannel;
        CloseUtils.close(channel);
        packetChannel = null;

        if (packet != null) {
            callback.onReceivePacketCompleted(packet);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int receiveSize;
        if (packetTemp == null) {
            receiveSize = 4;
        } else {
            receiveSize = (int) Math.min(args.capacity(), total - position);
        }
        args.limit(receiveSize);
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        assemblePacket(args);
        registerReceive();
    }

    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            completePacket(false);
        }
    }


}
