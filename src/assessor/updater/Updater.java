package assessor.updater;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Updater {

    public static void downloadFile(String fileURL, String destination, UpdateProgressDialog dialog) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int totalSize = connection.getContentLength();

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            int downloaded = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;

                int progress = (int) (((double) downloaded / totalSize) * 100);
                dialog.updateProgress(progress);
            }
        }
    }

    public static void restartApp(String launcherName) throws IOException {
        File launcher = new File(launcherName);

        if (launcher.exists()) {
            // Launch via EXE
            Runtime.getRuntime().exec(launcher.getAbsolutePath());
        } else {
            // Fallback to JAR launch
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            Runtime.getRuntime().exec(new String[]{javaBin, "-jar", "assessor-cert.jar"});
        }
        System.exit(0);
    }
}
