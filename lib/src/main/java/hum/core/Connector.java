package hum.core;

import hum.impl.SocketChannelAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * @author hum
 */
public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    private UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);

        this.sender = adapter;
        this.receiver = adapter;

        readNextMessage();
    }

    private void readNextMessage() {
        if (receiver != null) {
            try {
                receiver.receiveAsync(echoReceiveListener);
            } catch (IOException e) {
                System.out.println("read exception: " + e.getMessage());
            }
        }
    }

    private IoArgs.IoArgsEventListener echoReceiveListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {

            onReceiveNewMessage(args.bufferString());
            readNextMessage();
        }
    };

    /**
     * print to stdout
     *
     * @param str
     */
    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ":" + str);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }
}
