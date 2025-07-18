package tools;

import java.io.*;
import java.util.*;

public class VersionBumper {
    public static void main(String[] args) throws IOException {
        File versionFile = new File("version.properties");
        Properties props = new Properties();

        if (!versionFile.exists()) {
            System.err.println("version.properties not found.");
            return;
        }

        // Load current version
        try (FileReader reader = new FileReader(versionFile)) {
            props.load(reader);
        }

        int major = Integer.parseInt(props.getProperty("major", "1"));
        int minor = Integer.parseInt(props.getProperty("minor", "0"));
        int patch = Integer.parseInt(props.getProperty("patch", "0"));
        int versionCode = Integer.parseInt(props.getProperty("versionCode", "1"));

        // Increment version
        patch++;
        versionCode++;

        // Save updated values FIRST
        props.setProperty("patch", String.valueOf(patch));
        props.setProperty("versionCode", String.valueOf(versionCode));
        try (FileWriter writer = new FileWriter(versionFile)) {
            props.store(writer, "Auto-bumped version");
        }

        // Print updated version info
        String versionStr = String.format("%d.%d.%d", major, minor, patch);
        System.out.println("✔ Bumped version to " + versionStr + " (code " + versionCode + ")");
    }
}