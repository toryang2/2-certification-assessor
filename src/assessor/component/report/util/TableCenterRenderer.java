/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Toryang
 */
public class TableCenterRenderer extends DefaultTableCellRenderer {
        public TableCenterRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER); // Add this line
    }
}
