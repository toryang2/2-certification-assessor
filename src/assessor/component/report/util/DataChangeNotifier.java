package assessor.component.report.util;

import java.util.ArrayList;
import java.util.List;

public class DataChangeNotifier {
    private static final DataChangeNotifier instance = new DataChangeNotifier();
    private final List<DataChangeListener> listeners = new ArrayList<>();

    public interface DataChangeListener {
        void onDataChanged();
    }

    private DataChangeNotifier() {}

    public static DataChangeNotifier getInstance() {
        return instance;
    }

    public void addListener(DataChangeListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeListener(DataChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void notifyDataChange() {
        synchronized (listeners) {
            for (DataChangeListener listener : new ArrayList<>(listeners)) {
                listener.onDataChanged();
            }
        }
    }
}