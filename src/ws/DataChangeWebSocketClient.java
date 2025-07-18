package ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class DataChangeWebSocketClient extends WebSocketClient {
    private final Consumer<String> messageHandler;
    private volatile boolean keepRunning = true;

    public DataChangeWebSocketClient(String wsUri, Consumer<String> messageHandler) throws Exception {
        super(new URI(wsUri));
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
        reconnectForever();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        reconnectForever();
    }

    public void stopClient() {
        keepRunning = false;
        this.close();
    }

    public void reconnectForever() {
        if (!keepRunning) return;
        new Thread(() -> {
            while (keepRunning && !isOpen()) {
                try {
                    System.out.println("Attempting to reconnect WebSocket...");
                    reconnectBlocking();
                    if (isOpen()) {
                        System.out.println("Reconnected to server.");
                        break;
                    }
                } catch (Exception e) {
                    // ignore and keep retrying
                }
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }, "WS-Reconnect-Thread").start();
    }

    public void startWithAutoReconnect() {
        keepRunning = true;
        new Thread(() -> {
            while (keepRunning) {
                try {
                    if (!isOpen()) {
                        System.out.println("Trying to connect WebSocket...");
                        connectBlocking();
                    }
                    while (keepRunning && isOpen()) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    }
                } catch (Exception e) {
                    // ignore and retry
                }
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }, "WS-Startup-Thread").start();
    }
}