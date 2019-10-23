package hum;


import hum.constants.Constants;
import hum.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * udp provider
 *
 * @author hum
 */
public class ServerProvider {
    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("ServerProvider Started.");

            try {
                // listen on udp port, waiting search
                ds = new DatagramSocket(Constants.UDP_PORT_SERVER);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    ds.receive(receivePack);

                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    int minLen = Constants.HEADER.length + Constants.UDP_COMMAND_LENGTH + Constants.PORT_LENGTH;
                    boolean isValid = clientDataLen >= minLen && ByteUtils.startsWith(clientData, Constants.HEADER);

                    System.out.println("ServerProvider receive form ip:" + clientIp + "\tport:" + clientPort + "\tdataValid:" + isValid);

                    if (!isValid) {
                        continue;
                    }

                    int index = Constants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    // client udp port
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    if (cmd == Constants.UDP_SEARCH_COMMAND && responsePort > 0) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(Constants.HEADER);
                        byteBuffer.putShort(Constants.UDP_RESPONSE_COMMAND);
                        // response tcp port to client
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        DatagramPacket responsePacket = new DatagramPacket(buffer, len, receivePack.getAddress(), responsePort);
                        ds.send(responsePacket);
                        System.out.println("ServerProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        System.out.println("ServerProvider receive cmd nonsupport, cmd:" + cmd + "\tport:" + port);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }

            System.out.println("ServerProvider Finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
