package assessor.component.report.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Arrays;

public class ReportLoader {
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
    }

    public void clearCache() {
        SwingUtilities.invokeLater(() -> {
            model.setColumnIdentifiers(new Object[0]);
            model.setRowCount(0);
        });
    }

    public void loadData() {
        if (refreshInProgress) return;
        refreshInProgress = true;

        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            model.setColumnIdentifiers(new Object[]{"Loading..."});
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                SwingUtilities.invokeLater(callbacks::onLoadStart);
                
                try (Connection conn = DriverManager.getConnection(
                        ConfigHelper.getDbUrl(),
                        ConfigHelper.getDbUser(),
                        ConfigHelper.getDbPassword());
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id AS ID, Type AS Type, Patient, ParentGuardian, ParentGuardian2, Hospital,"
                             + " HospitalAddress, Barangay, CertificationDate, CertificationTime,"
                             + " AmountPaid, ReceiptNo, ReceiptDateIssued, PlaceIssued, Signatory"
                             + " FROM reports ORDER BY id DESC")) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    String[] columns = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        columns[i] = meta.getColumnName(i + 1);
                    }

                    Object[][] data = new Object[100][colCount];
                    int rowCount = 0;
                    while (rs.next()) {
                        if (rowCount >= data.length) {
                            data = Arrays.copyOf(data, data.length * 2);
                        }
                        for (int col = 0; col < colCount; col++) {
                            data[rowCount][col] = rs.getObject(col + 1);
                        }
                        rowCount++;
                    }

                    final String[] finalColumns = columns;
                    final Object[][] finalData = Arrays.copyOf(data, rowCount);

                    SwingUtilities.invokeLater(() -> {
                        model.setColumnIdentifiers(finalColumns);
                        model.fireTableStructureChanged(); // Critical change
                        model.setRowCount(0);
                        for (Object[] row : finalData) {
                            model.addRow(row);
                        }
                        callbacks.onLoadComplete();
                    });

                } catch (SQLException ex) {
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
                refreshInProgress = false;
                try {
                    get();
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> 
                        callbacks.onLoadError(ex.getMessage()));
                }
            }
        }.execute();
    }

    public boolean hasActiveRefresh() {
        return refreshInProgress;
    }
}