package ws;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataChangeWebSocketServer extends WebSocketServer {
    private static final int PORT = 8887;
    private static final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

    public DataChangeWebSocketServer() {
        super(new InetSocketAddress(PORT));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message + " from " + conn.getRemoteSocketAddress());
        if ("DATA_CHANGED".equals(message)) {
            broadcastDataChange();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
//        System.out.println("WebSocket server started on ws://0.0.0.0:" + PORT);
        InetSocketAddress address = getAddress(); // gets the actual bound address
        if (address != null) {
            System.out.println("WebSocket server started on ws://" + address.getHostString() + ":" + address.getPort());
        } else {
            System.out.println("WebSocket server started, but could not determine address.");
        }
    }

    public void broadcastDataChange() {
        System.out.println("Broadcasting DATA_CHANGED to " + connections.size() + " clients.");
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send("DATA_CHANGED");
            }
        }
    }
}