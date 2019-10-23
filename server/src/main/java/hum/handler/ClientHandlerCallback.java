package hum.handler;

/**
 * @author hum
 */
public interface ClientHandlerCallback {
    void onSelfClosed(ClientHandler handler);

    void onNewMessageArrived(ClientHandler handler, String msg);
}
