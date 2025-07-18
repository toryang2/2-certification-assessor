package assessor.component.report.util;

import ws.DataChangeWebSocketClient;
import java.util.ArrayList;
import java.util.List;

public class DataChangeNotifier {
    private static DataChangeNotifier instance;
    private final List<DataChangeListener> listeners = new ArrayList<>();
    private DataChangeWebSocketClient wsClient;
    private Runnable onReconnectStart;
    private Runnable onReconnectSuccess;

    public static DataChangeNotifier getInstance() {
        if (instance == null) {
            instance = new DataChangeNotifier();
        }
        return instance;
    }

    public interface DataChangeListener {
        void onDataChanged();
    }

    public void setOnReconnectStart(Runnable callback) {
        this.onReconnectStart = callback;
    }
    public void setOnReconnectSuccess(Runnable callback) {
        this.onReconnectSuccess = callback;
    }
    
    public void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyDataChange() {
        // Notify local listeners
        synchronized (listeners) {
            for (DataChangeListener listener : new ArrayList<>(listeners)) {
                listener.onDataChanged();
            }
        }
        // Send notification to server
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.send("DATA_CHANGED");
            System.out.println("Sent DATA_CHANGED to server");
        }
    }

    public void connectWebSocket(String wsUri) {
        try {
            wsClient = new DataChangeWebSocketClient(wsUri, msg -> notifyRemoteDataChange());
            wsClient.connect();
            System.out.println("Connecting to WebSocket server: " + wsUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyRemoteDataChange() {
        System.out.println("Remote data change received.");
        fireRemoteDataChange();
    }

    public void fireRemoteDataChange() {
        synchronized (listeners) {
            for (DataChangeListener listener : new ArrayList<>(listeners)) {
                listener.onDataChanged();
            }
        }
    }
}