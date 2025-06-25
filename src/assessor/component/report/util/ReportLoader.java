package assessor.component.report.util;

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
//            model.setColumnIdentifiers(new Object[]{"Loading..."}); // Set temporary "Loading..." header
            callbacks.onLoadStart();
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                String query = "SELECT id AS ID, Type AS Type, Patient, ParentGuardian, ParentGuardian2, Hospital,"
                             + " HospitalAddress, Barangay, CertificationDate, CertificationTime,"
                             + " AmountPaid, ReceiptNo, ReceiptDateIssued, PlaceIssued, Signatory, LegalAge"
                             + " FROM reports ORDER BY id DESC";
                logger.log(Level.INFO, "Executing SQL query: {0}", query);

                try (Connection conn = DriverManager.getConnection(
                        ConfigHelper.getDbUrl(),
                        ConfigHelper.getDbUser(),
                        ConfigHelper.getDbPassword());
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    String[] columns = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        columns[i] = meta.getColumnName(i + 1);
                    }
                    logger.log(Level.INFO, "Retrieved {0} columns from database.", colCount);

                    Object[][] data = new Object[100][colCount];
                    int rowCount = 0;
                    while (rs.next()) {
                        if (rowCount >= data.length) {
                            Object[][] newData = new Object[data.length * 2][colCount];
                            System.arraycopy(data, 0, newData, 0, data.length);
                            data = newData;
                        }
                        for (int col = 0; col < colCount; col++) {
                            data[rowCount][col] = rs.getObject(col + 1);
                        }
                        rowCount++;
                    }
                    logger.log(Level.INFO, "Retrieved {0} rows from database.", rowCount);

                    final String[] finalColumns = columns;
                    final Object[][] finalData = Arrays.copyOf(data, rowCount);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            logger.log(Level.INFO, "Updating table model with retrieved data.");
                            model.setColumnIdentifiers(finalColumns); // Set headers correctly
                            model.setRowCount(0);
                            for (Object[] row : finalData) {
                                model.addRow(row);
                            }
                            callbacks.onLoadComplete();
                            if (onComplete != null) {
                                logger.log(Level.INFO, "Running onComplete callback.");
                                onComplete.run();
                            }
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error updating table model: {0}", e.getMessage());
                            e.printStackTrace();
                        } finally {
                            refreshInProgress = false; // Reset flag after data load
                            logger.log(Level.INFO, "Refresh flag reset.");
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

    public boolean hasActiveRefresh() {
        return refreshInProgress;
    }
}