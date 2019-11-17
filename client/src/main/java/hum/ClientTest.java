package hum;

import hum.bean.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hum
 */
public class ClientTest {
    private static boolean done;

    public static void main(String[] args) throws IOException {
        File cachePath = Foo.getCacheDir("client/test");
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if (info == null) {
            return;
        }

        int size = 0;
        final List<TcpClient> tcpClients = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                TcpClient tcpClient = TcpClient.startWith(info, cachePath);
                if (tcpClient == null) {
                    System.out.println("connect exception");
                    continue;
                }
                tcpClients.add(tcpClient);
                System.out.println("connect success：" + (++size));

            } catch (IOException e) {
                System.out.println("connect exception");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.in.read();

        Runnable runnable = () -> {
            while (!done) {
                for (TcpClient tcpClient : tcpClients) {
                    tcpClient.send("Hello");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();

        done = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (TcpClient tcpClient : tcpClients) {
            tcpClient.exit();
        }

    }
}
