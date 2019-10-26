package hum;


import hum.handler.ClientHandler;
import hum.handler.ClientHandlerCallback;
import hum.utils.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hum
 */
public class TcpServer implements ClientHandlerCallback {
    private final int port;
    private ClientListener listener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public TcpServer(int port) {
        this.port = port;
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            selector = Selector.open();

            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT);

            this.serverSocketChannel = server;
            System.out.println("tcp server info：" + server.getLocalAddress().toString());

            ClientListener listener = new ClientListener();
            this.listener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }

        CloseUtils.close(serverSocketChannel);
        CloseUtils.close(selector);

        synchronized (TcpServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }

            clientHandlerList.clear();
        }
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(final ClientHandler handler, final String msg) {
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TcpServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        continue;
                    }
                    clientHandler.send(msg);
                }
            }
        });
    }

    /**
     * wait client connect
     */
    private class ClientListener extends Thread {
        private boolean done = false;


        @Override
        public void run() {
            super.run();
            Selector selector = TcpServer.this.selector;
            System.out.println("tcp server start");
            do {
                try {
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }

                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            try {
                                ClientHandler clientHandler = new ClientHandler(socketChannel, TcpServer.this);
                                synchronized (TcpServer.this) {
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("client handler exception" + e.getMessage());
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!done);

            System.out.println("tcp server end");
        }

        void exit() {
            done = true;
            selector.wakeup();
        }
    }

}
