/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import javax.swing.*;
import java.awt.event.*;

public class KeyboardRefresh {
    // Implementation not affecting buttons
    public KeyboardRefresh(ReportLoader loader, JFrame frame) {
        JRootPane rootPane = frame.getRootPane();
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(f5, "keyboardRefresh");
        rootPane.getActionMap().put("keyboardRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!loader.hasActiveRefresh()) {
                    loader.loadData();
                }
            }
        });
    }
}
