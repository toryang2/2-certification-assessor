/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package assessor;

import java.io.*;
import assessor.component.report.util.ConfigHelper;
import assessor.component.report.util.DataChangeNotifier;
import assessor.updater.UpdateProgressDialog;
import assessor.utils.DemoPreferences;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.util.FontUtils;
import java.awt.*;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 *
 * @author Toryang
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        createConfigIfMissing();
        setupLookAndFeel();
        if (checkAndUpdateIfNeeded()) {
            return; // Exit if updater restarts the app
        }

        launchUI();
        String serverIp = ConfigHelper.getDbServer();
        DataChangeNotifier.getInstance().connectWebSocket("ws://" + serverIp + ":8887");
        
        
        // Add a sample listener
        DataChangeNotifier.getInstance().addListener(() ->
            System.out.println("Data changed! (Listener notified)")
        );
        // Simulate a local data change after 5 seconds
        try {
            Thread.sleep(5000);
            System.out.println("Simulating local data change...");
            DataChangeNotifier.getInstance().notifyDataChange();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Database URL: " + ConfigHelper.getDbUrl());
        System.out.println("User: " + ConfigHelper.getDbUser());
    }
    
    private static void createConfigIfMissing() {
        File configFile = new File("config.ini");
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(
                    "[Database]\n" +
                    "Server=192.168.1.33;\n" +
                    "Port=3306;\n" +
                    "Database=certificationdb;\n" +
                    "User ID=user;\n" +
                    "Password=user;\n"
                );
                System.out.println("Created new config.ini with default values");
            } catch (Exception e) {
                System.err.println("Error creating config file:");
                e.printStackTrace();
            }
        }
    }
    
//    private static void launchUI() {
//        DemoPreferences.init();
////        DemoPreferences.clearPreferences();
//        FlatRobotoFont.install();
//        FlatLaf.registerCustomDefaultsSource("assessor.themes");
//        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
//        DemoPreferences.setupLaf();
//        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
//    }
    
    
    private static void setupLookAndFeel() {
        DemoPreferences.init();
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("assessor.themes");
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        DemoPreferences.setupLaf();
    }

    private static void launchUI() {
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
    private static boolean checkAndUpdateIfNeeded() {
        try {
            // Get current app version from MainFrame
            int[] localVersion = assessor.updater.VersionHelper.parseVersionString(MainFrame.VERSION);

            String server = ConfigHelper.getDbServer();
            String versionUrl = "http://" + server + "/app/version.properties";
            String jarUrl = "http://" + server + "/app/assessor-cert.jar";
            String exeUrl = "http://" + server + "/app/launcher.exe";  // New EXE URL
            System.out.println("🌐 Fetching version from: " + versionUrl);

            int[] serverVersion = assessor.updater.VersionHelper.loadVersion(new java.net.URL(versionUrl));
            System.out.println("🌐 Server version: " + assessor.updater.VersionHelper.versionString(serverVersion));

            if (assessor.updater.VersionHelper.isNewer(localVersion, serverVersion)) {
                System.out.println("⬆ Update available!");
                int choice = JOptionPane.showConfirmDialog(null,
                        "A new version " + assessor.updater.VersionHelper.versionString(serverVersion) +
                        " is available.\nDo you want to update now?",
                        "Update Available", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    UpdateProgressDialog dialog = new UpdateProgressDialog(null);

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            try {
                                assessor.updater.Updater.downloadFile(jarUrl, "assessor-cert.jar", dialog);
                                assessor.updater.Updater.downloadFile(exeUrl, "launcher.exe", dialog);  // New download
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(null,
                                        "Download failed: " + e.getMessage(),
                                        "Update Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            dialog.dispose();
                            try {
                                JOptionPane.showMessageDialog(null, "Update complete. Restarting...");
                                assessor.updater.Updater.restartApp("launcher.exe");
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null,
                                        "Failed to restart app: " + e.getMessage(),
                                        "Update Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        }
                    };

                    worker.execute();
                    dialog.setVisible(true);
                    return true;
                }
            } else {
                System.out.println("✅ App is up to date.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error checking for updates:\n" + e.getMessage(),
                    "Update Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}
