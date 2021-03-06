package hum;


import hum.bean.ServerInfo;
import hum.core.Connector;
import hum.core.Packet;
import hum.core.ReceivePacket;
import hum.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

/**
 * @author hum
 */
public class TcpClient extends Connector {
    private final File cachePath;

    public TcpClient(SocketChannel socketChannel, File cachePath) throws IOException {
        this.cachePath = cachePath;
        setup(socketChannel);
    }

    public static TcpClient startWith(ServerInfo info, File cachePath) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("connect start");
        System.out.println("client info：" + socketChannel.getLocalAddress().toString());
        System.out.println("server info：" + socketChannel.getRemoteAddress().toString());

        try {
            return new TcpClient(socketChannel, cachePath);
        } catch (Exception e) {
            System.out.println("exception close!");
            CloseUtils.close(socketChannel);
        }
        return null;
    }

    @Override
    protected void onReceivedPacket(ReceivePacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
        }
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("connect closed, can not read data");
    }

    public void exit() {
        CloseUtils.close(this);

    }

}
