package assessor.updater;

import javax.swing.*;
import java.awt.*;

public class UpdateProgressDialog extends JDialog {
    private final JProgressBar progressBar;

    public UpdateProgressDialog(Frame parent) {
        super(parent, "Downloading Update", true);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JLabel label = new JLabel("Downloading update...");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }
}
