package assessor.component.chart;

import assessor.auth.SessionManager;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class CustomTableModel extends DefaultTableModel {
    private static final int CHECKBOX_COLUMN = 0;
    
    public CustomTableModel() {
        super();
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        if (column == CHECKBOX_COLUMN && SessionManager.getInstance().isAdmin()) {
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        // Checkbox column is always editable for admin
        if (column == CHECKBOX_COLUMN && SessionManager.getInstance().isAdmin()) {
            return true;
        }
        // Other columns are only editable for admin
        return SessionManager.getInstance().isAdmin();
    }
    
    // Method to add the checkbox column
    public void addCheckboxColumn() {
        // Only add checkbox column for admin users
        if (SessionManager.getInstance().isAdmin()) {
            Vector<Object> columnIds = new Vector<>();
            columnIds.add("Select"); // Checkbox column
            
            // Add existing column identifiers
            for (int i = 0; i < getColumnCount(); i++) {
                columnIds.add(getColumnName(i));
            }
            
            // Create data with checkboxes
            Vector<Vector<Object>> newData = new Vector<>();
            for (int i = 0; i < getRowCount(); i++) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(Boolean.FALSE); // Add checkbox
                
                // Add existing row data
                for (int j = 0; j < getColumnCount(); j++) {
                    rowData.add(getValueAt(i, j));
                }
                newData.add(rowData);
            }
            
            // Set new data and column identifiers
            setDataVector(newData, columnIds);
        }
    }
}