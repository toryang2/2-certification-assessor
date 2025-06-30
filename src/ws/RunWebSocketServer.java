package ws;

public class RunWebSocketServer {
    public static void main(String[] args) {
        DataChangeWebSocketServer server = new DataChangeWebSocketServer();
        server.start();
        System.out.println("Certification WebSocket server running...");
    }
}