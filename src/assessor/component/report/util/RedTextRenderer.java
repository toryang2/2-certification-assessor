/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;


import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author user
 */
public class RedTextRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Get the default cell renderer component
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Set text color to red
        component.setForeground(Color.RED);
        setHorizontalAlignment(SwingConstants.RIGHT);

        return component;
    }
}