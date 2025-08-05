package assessor;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(79, 164, 171));
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Logo
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/assessor/icons/splash.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(620, 300, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(logo, BorderLayout.CENTER);
        
        // Status panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        statusLabel = new JLabel("Starting application...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        progressBar = new JProgressBar();
//        progressBar.setIndeterminate(true);
        progressBar.setIndeterminate(false); // determinate mode
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0); // start at 0
        
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        content.add(bottomPanel, BorderLayout.SOUTH);
        
        getContentPane().add(content);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void close() {
        dispose();
    }
    
    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }
}