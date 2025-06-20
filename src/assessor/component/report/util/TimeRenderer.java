/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.SwingConstants;
/**
 *
 * @author Toryang
 */
public class TimeRenderer extends DefaultTableCellRenderer {
    private final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

    public TimeRenderer() {
//        setHorizontalAlignment(SwingConstants.RIGHT); // Add this line
    }
    
    @Override
    protected void setValue(Object value) {
        if (value instanceof Date) {
            setText(sdf.format((Date) value));
        } else {
            super.setValue(value);
        }
    }
}