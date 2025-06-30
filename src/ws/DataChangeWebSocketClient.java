package ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class DataChangeWebSocketClient extends WebSocketClient {
    private final Runnable notifyCallback;

    public DataChangeWebSocketClient(String wsUri, Runnable notifyCallback) throws Exception {
        super(new URI(wsUri));
        this.notifyCallback = notifyCallback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        if ("DATA_CHANGED".equals(message) && notifyCallback != null) {
            notifyCallback.run();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}