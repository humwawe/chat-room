package hum;


import hum.bean.ServerInfo;
import hum.box.FileSendPacket;
import hum.core.IoContext;
import hum.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hum
 */
public class Client {
    public static void main(String[] args) throws IOException {
        File cachePath = Foo.getCacheDir("client");
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();

        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TcpClient tcpClient = null;
            try {
                tcpClient = TcpClient.startWith(info, cachePath);
                if (tcpClient == null) {
                    return;
                }
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
        IoContext.close();
    }

    private static void write(TcpClient tcpClient) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        do {
            String str = input.readLine();
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
            // --f url
            if (str.startsWith("--f")) {
                String[] array = str.split(" ");
                if (array.length >= 2) {
                    String filePath = array[1];
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        FileSendPacket packet = new FileSendPacket(file);
                        tcpClient.send(packet);
                        continue;
                    }
                }
            }
            tcpClient.send(str);

        } while (true);
    }
}
