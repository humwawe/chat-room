package hum;


import hum.constants.Constants;
import hum.core.IoContext;
import hum.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hum
 */
public class Server {
    public static void main(String[] args) throws IOException {
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();
        TcpServer tcpServer = new TcpServer(Constants.TCP_PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Tcp server start failed!");
            return;
        }

        ServerProvider.start(Constants.TCP_PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        ServerProvider.stop();
        tcpServer.stop();
        IoContext.close();
    }
}
