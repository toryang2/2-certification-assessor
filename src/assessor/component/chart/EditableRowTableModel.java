package assessor.component.chart;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * EditableRowTableModel adds a virtual "Edit?" checkbox column to a normal table model,
 * allowing per-row edit control without modifying underlying data for reports/DB.
 */
public class EditableRowTableModel extends AbstractTableModel {
    private final String[] dataColumns;
    private final List<Object[]> data = new ArrayList<>();
    private final List<Boolean> editFlags = new ArrayList<>();

    public EditableRowTableModel(String[] dataColumns) {
        this.dataColumns = dataColumns;
    }

    public void setData(List<Object[]> rows) {
        data.clear();
        editFlags.clear();
        for (Object[] r : rows) {
            data.add(r);
            editFlags.add(Boolean.FALSE); // default unchecked
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return dataColumns.length + 1; // +1 for checkbox
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "Edit?";
        return dataColumns[column - 1];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return editFlags.get(rowIndex);
        return data.get(rowIndex)[columnIndex - 1];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            editFlags.set(rowIndex, (Boolean) aValue);
        } else {
            data.get(rowIndex)[columnIndex - 1] = aValue;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Replace SessionManager.getInstance().isAdmin() with your actual admin check if needed,
        // or expose a setter for a boolean flag.
        boolean isAdmin = assessor.auth.SessionManager.getInstance().isAdmin();
        if (!isAdmin) return false;
        if (columnIndex == 0) return true; // The checkbox
        return Boolean.TRUE.equals(editFlags.get(rowIndex)); // Only editable if checked
    }

    /** Add a single row (for manual row addition). */
    public void addRow(Object[] row) {
        data.add(row);
        editFlags.add(Boolean.FALSE);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    /** Get pure data rows (no checkbox column). */
    public List<Object[]> getPureData() {
        return new ArrayList<>(data);
    }

    /** Clear all data and edit flags. */
    public void clear() {
        data.clear();
        editFlags.clear();
        fireTableDataChanged();
    }
}