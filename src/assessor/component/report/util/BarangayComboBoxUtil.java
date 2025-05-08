package assessor.component.report.util;

import javax.swing.*;
import java.util.List;

public class BarangayComboBoxUtil {
    public static void populateBarangayComboBox(JComboBox<String> comboBox) {
        // Fetch barangays from the database
        List<String> barangays = DatabaseSaveHelper.fetchBarangays();

        // Clear existing items in the combo box
        comboBox.removeAllItems();

        // Add barangays to the combo box
        for (String barangay : barangays) {
            comboBox.addItem(barangay);
        }
    }
}