package assessor;

import assessor.auth.Authenticator;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import assessor.component.report.util.ConfigHelper;
import assessor.component.report.util.DataChangeNotifier;
import assessor.updater.UpdateProgressDialog;
import assessor.utils.ChangelogHelper;
import assessor.utils.DemoPreferences;
import assessor.utils.MarkdownHelper;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.util.FontUtils;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import java.util.logging.Logger;

public class Main {    
    private static final Logger LOGGER = assessor.utils.AdvancedLogger.getLogger(Main.class.getName());
        private static boolean exitingForUpdate = false;

    private static class InitializationWorker extends SwingWorker<Void, String> {
        private final SplashScreen splash;
        private final boolean checkUpdates;

        public InitializationWorker(SplashScreen splash, boolean checkUpdates) {
            this.splash = splash;
            this.checkUpdates = checkUpdates;
        }

        @Override
        protected Void doInBackground() {
            publish("Loading configuration...");
            splash.setProgress(10);
            createConfigIfMissing();
            
            publish("Initializing database...");
            splash.setProgress(20);
            Authenticator.initializeDriver();
            
            publish("Scanning /lib directory...");
            splash.setProgress(30);
            
            String server = ConfigHelper.getDbServer();
            String libBaseUrl = "http://" + server + "/app/lib";
            File libDir = new File("lib");
            if (!libDir.exists()) libDir.mkdirs();

            List<String> remoteFiles = fetchLibFileList(libBaseUrl);
            List<String> localFiles = fetchLocalLibFiles("lib");

            java.util.Set<String> remoteSet = new java.util.HashSet<>(remoteFiles);
            java.util.Set<String> localSet = new java.util.HashSet<>(localFiles);

            java.util.Set<String> missingLocally = new java.util.HashSet<>(remoteSet);
            missingLocally.removeAll(localSet);

            java.util.Set<String> extraLocally = new java.util.HashSet<>(localSet);
            extraLocally.removeAll(remoteSet);

            java.util.Set<String> synced = new java.util.HashSet<>(remoteSet);
            synced.retainAll(localSet);

            for (String f : synced) System.out.println("✅ Present: " + f);
            for (String f : extraLocally) System.out.println("🧹 Extra local: " + f);

            publish("Downloading missing /lib files...");
            int i = 0;
            for (String file : missingLocally) {
                publish("⬇ Downloading: " + file);
                try {
                    URL url = new URL(libBaseUrl + "/" + file);
                    File outFile = new File(libDir, file);
                    try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
                    }
                    System.out.println("✅ Downloaded: " + file);
                } catch (IOException e) {
                    System.err.println("❌ Failed to download " + file + ": " + e.getMessage());
                }

                splash.setProgress(30 + (++i * 20 / missingLocally.size())); // up to ~50
            }
            
            publish("Checking for updates...");
            splash.setProgress(90);
            if (checkUpdates && checkAndUpdateIfNeeded()) {
                Main.exitingForUpdate = true;
                return null;
            }
            
            publish("Loading application...");
            splash.setProgress(100);
            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            String lastMessage = chunks.get(chunks.size() - 1);
            splash.updateStatus(lastMessage);
        }

        @Override
        protected void done() {
            splash.close();
        }
    }
    
    public static void main(String[] args) {
        // Force macOS Light LAF before splash
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.themes.FlatMacLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf macOS Light theme");
            e.printStackTrace();
        }
        
        SplashScreen splash = new SplashScreen();
        splash.updateStatus("Starting application...");
        
        setupLookAndFeel();
        
        boolean shouldCheckUpdates = true;
        InitializationWorker worker = new InitializationWorker(splash, shouldCheckUpdates);
        worker.execute();

        worker.addPropertyChangeListener(evt -> {
            if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
                try {
                    worker.get();
                    
                    if (!Main.exitingForUpdate && !System.getProperty("app.restarting", "false").equals("true")) {
                        EventQueue.invokeLater(() -> {
                            MainFrame frame = new MainFrame();
                            frame.setVisible(true);
                            
                            String serverIp = ConfigHelper.getDbServer();
                            DataChangeNotifier.getInstance().connectWebSocket("ws://" + serverIp + ":8887");
                            
                            DataChangeNotifier.getInstance().addListener(() -> 
                                System.out.println("Data changed! (Listener notified)")
                            );
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "Initialization failed: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        });
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
    
    private static void setupLookAndFeel() {
        DemoPreferences.init();
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("assessor.themes");
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        DemoPreferences.setupLaf();
    }
    
    private static boolean checkAndUpdateIfNeeded() {
        try {
            int[] localVersion = assessor.updater.VersionHelper.parseVersionString(MainFrame.VERSION);
            String server = ConfigHelper.getDbServer();
            String versionUrl = "http://" + server + "/app/version.properties";
            String jarUrl = "http://" + server + "/app/assessor-cert.jar";
            String exeUrl = "http://" + server + "/app/launcher.exe";
            System.out.println("🌐 Fetching version from: " + versionUrl);

            int[] serverVersion = assessor.updater.VersionHelper.loadVersion(new java.net.URL(versionUrl));
            System.out.println("🌐 Server version: " + assessor.updater.VersionHelper.versionString(serverVersion));

            if (assessor.updater.VersionHelper.isNewer(localVersion, serverVersion)) {
                System.out.println("⬆ Update available!");
                
                File cachedChangelog = new File("changelog.md");
                String changelogText = "";
                try {
                    java.net.URL changelogURL = new java.net.URL("http://" + server + "/app/changelog.md");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(changelogURL.openStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        boolean started = false;
                        
                        // Only get the first version block (topmost section)
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().startsWith("## ")) {
                                if (started) break;
                                started = true;
                            }
                            if (started) {
                                sb.append(line).append("\n");
                            }
                        }
                        
                        changelogText = sb.toString().trim();
                        
                        // ✅ Cache locally
                        try (Writer writer = new OutputStreamWriter(new FileOutputStream(cachedChangelog), StandardCharsets.UTF_8)) {
                            writer.write(changelogText);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();

                    // Try loading cached version
                    if (cachedChangelog.exists()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cachedChangelog), StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            changelogText = sb.toString().trim() + "\n\n(Loaded from cache)";
                        } catch (IOException ignored) {
                        }
                    } else {
                        changelogText = "⚠ Unable to fetch changelog.";
                    }
                }
                // Original
//                int choice = JOptionPane.showConfirmDialog(null,
//                        "A new version " + assessor.updater.VersionHelper.versionString(serverVersion) +
//                        " is available.\nDo you want to update now?",
//                        "Update Available", JOptionPane.YES_NO_OPTION);
                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollPane = ChangelogHelper.createChangelogPane(changelogText);
                scrollPane.setPreferredSize(new Dimension(320, 120));
                scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(scrollPane);
                
                JLabel confirmLabel = new JLabel("Do you want to update now?");
                confirmLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(confirmLabel);

                // Show dialog
                int choice = JOptionPane.showConfirmDialog(null,
                        content,
                        "Update Available – Version " + assessor.updater.VersionHelper.versionString(serverVersion),
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    UpdateProgressDialog dialog = new UpdateProgressDialog(null);

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            try {
                                assessor.updater.Updater.downloadFile(jarUrl, "assessor-cert.jar", dialog);
                                assessor.updater.Updater.downloadFile(exeUrl, "launcher.exe", dialog);
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
                                System.setProperty("app.restarting", "true");
                                Main.exitingForUpdate = true;
                                assessor.updater.Updater.restartApp("launcher.exe");
                                System.exit(0); // <-- actually exit
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
    
    private static List<String> fetchLocalLibFiles(String libPath) {
        List<String> list = new ArrayList<>();
        File libDir = new File(libPath);
        if (libDir.exists() && libDir.isDirectory()) {
            File[] files = libDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        list.add(f.getName());
                    }
                }
            }
        }
        return list;
    }
    
    // Fetch all file links from /lib directory HTML
    private static List<String> fetchLibFileList(String libUrl) {
        List<String> fileList = new ArrayList<>();
        try {
            URL url = new URL(libUrl + "/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<a href=\"")) {
                        int start = line.indexOf("<a href=\"") + 9;
                        int end = line.indexOf("\"", start);
                        if (start > 8 && end > start) {
                            String filename = line.substring(start, end);

                            // Filter out directories and sorting links like ?C=N;O=D
                            if (!filename.endsWith("/") && !filename.startsWith("?")) {
                                fileList.add(filename);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to fetch /lib index: " + e.getMessage());
        }
        return fileList;
    }
}