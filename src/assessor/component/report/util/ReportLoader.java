package assessor.component.report.util;

import assessor.auth.SessionManager;
import assessor.component.report.util.ConfigHelper;
import assessor.component.report.util.DataChangeNotifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportLoader {
    private static final Logger logger = Logger.getLogger(ReportLoader.class.getName());

    private volatile boolean refreshInProgress = false;
    private final DefaultTableModel model;
    private final LoadCallbacks callbacks;
    private Timer pollingTimer;
    private long lastMaxId = -1;

    public interface LoadCallbacks {
        void onLoadStart();
        void onLoadComplete();
        void onLoadError(String message);
    }

    public ReportLoader(DefaultTableModel model, LoadCallbacks callbacks) {
        this.model = model;
        this.callbacks = callbacks;

        DataChangeNotifier.getInstance().addListener(this::onDataChanged);
    }

    private void onDataChanged() {
        // Clear cache and reload data when notified
        SwingUtilities.invokeLater(() -> {
            logger.log(Level.INFO, "Data change detected. Reloading table data...");
            loadData(null);
        });
    }

    public void cleanup() {
        // Unsubscribe from notifications when the loader is no longer needed
        DataChangeNotifier.getInstance().removeListener(this::onDataChanged);
    }

    public void clearCache() {
        SwingUtilities.invokeLater(() -> {
            logger.log(Level.INFO, "Clearing table cache...");
            model.setColumnIdentifiers(new Object[0]);
            model.setRowCount(0);
            logger.log(Level.INFO, "Cache cleared.");
        });
    }

    public void loadData(Runnable onComplete) {
        if (refreshInProgress) {
            logger.log(Level.WARNING, "Data refresh already in progress. Skipping...");
            return;
        }
        refreshInProgress = true;

        logger.log(Level.INFO, "Starting data load...");
        SwingUtilities.invokeLater(() -> {
            logger.log(Level.INFO, "Setting temporary 'Loading...' header in table.");
            model.setRowCount(0);
            callbacks.onLoadStart();
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                String query = "SELECT id AS ID, type AS Type, patient, hospital," // Patient, ParentGuardian, ParentGuardian2, re add if necessary
                        + " hospital_address, barangay, contact_no, certification_date, certification_time,"
                        + " amount_paid, receipt_no, receipt_date_issued, place_issued, signatory, legal_age"
                        + " FROM reports ORDER BY id DESC";
                logger.log(Level.INFO, "Executing SQL query: {0}", query);

                boolean isAdmin = SessionManager.getInstance().getAccessLevel() == 1;

                try (Connection conn = DriverManager.getConnection(
                        ConfigHelper.getDbUrl(),
                        ConfigHelper.getDbUser(),
                        ConfigHelper.getDbPassword());
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    int realColCount = colCount + (isAdmin ? 1 : 0);

                    String[] columns = new String[realColCount];
                    int colOffset = 0;
                    if (isAdmin) {
                        columns[0] = "Select";
                        colOffset = 1;
                    }
                    for (int i = 0; i < colCount; i++) {
                        columns[i + colOffset] = meta.getColumnName(i + 1);
                    }
                    logger.log(Level.INFO, "Retrieved {0} columns from database.", colCount);

                    Object[][] data = new Object[100][realColCount];
                    int rowCount = 0;
                    while (rs.next()) {
                        if (rowCount >= data.length) {
                            Object[][] newData = new Object[data.length * 2][realColCount];
                            System.arraycopy(data, 0, newData, 0, data.length);
                            data = newData;
                        }
                        int dataOffset = 0;
                        if (isAdmin) {
                            data[rowCount][0] = Boolean.FALSE; // Add unchecked box as first column
                            dataOffset = 1;
                        }
                        for (int col = 0; col < colCount; col++) {
                            data[rowCount][col + dataOffset] = rs.getObject(col + 1);
                        }
                        rowCount++;
                    }
                    logger.log(Level.INFO, "Retrieved {0} rows from database.", rowCount);

                    final String[] finalColumns = columns;
                    final Object[][] finalData = Arrays.copyOf(data, rowCount);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Save current column names
                            String[] currentColumns = new String[model.getColumnCount()];
                            for (int i = 0; i < model.getColumnCount(); i++) {
                                currentColumns[i] = model.getColumnName(i);
                            }
                            // Compare with new columns
                            boolean columnsChanged = !Arrays.equals(currentColumns, finalColumns);

                            if (columnsChanged) {
                                model.setDataVector(finalData, finalColumns);
                            } else {
                                // Only update rows
                                model.setRowCount(0);
                                for (Object[] row : finalData) {
                                    model.addRow(row);
                                }
                            }
                            callbacks.onLoadComplete();
                            if (onComplete != null) onComplete.run();
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error updating table model: {0}", e.getMessage());
                            callbacks.onLoadError(e.getMessage());
                        } finally {
                            refreshInProgress = false;
                        }
                    });
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Database error during data load: {0}", ex.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        callbacks.onLoadError(ex.getMessage());
                        JOptionPane.showMessageDialog(null,
                                "Database Error: " + ex.getMessage(),
                                "Connection Failed",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error in background data load: {0}", ex.getMessage());
                    SwingUtilities.invokeLater(() -> callbacks.onLoadError(ex.getMessage()));
                } finally {
                    refreshInProgress = false; // Ensure flag is reset
                    logger.log(Level.INFO, "Refresh flag reset in 'done' method.");
                }
            }
        }.execute();
    }

    public void startPolling(int intervalMillis) {
        if (pollingTimer != null) return;
        pollingTimer = new Timer(intervalMillis, e -> checkForUpdates());
        pollingTimer.setRepeats(true);
        pollingTimer.start();
    }

    public void stopPolling() {
        if (pollingTimer != null) {
            pollingTimer.stop();
            pollingTimer = null;
            logger.log(Level.INFO, "Polling stopped.");
        }
    }

    private void checkForUpdates() {
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM reports")) {
            if (rs.next()) {
                long maxID = rs.getLong(1);
                if (maxID != lastMaxId) {
                    lastMaxId = maxID;
                    SwingUtilities.invokeLater(() -> loadData(null));
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error checking for updates: {0}", ex.getMessage());
        }
    }

    public boolean hasActiveRefresh() {
        return refreshInProgress;
    }
}