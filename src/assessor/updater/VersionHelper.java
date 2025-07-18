package assessor.updater;

import java.io.*;
import java.net.*;
import java.util.Properties;

public class VersionHelper {

    public static int[] loadVersion(File file) throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
            return new int[]{
                Integer.parseInt(props.getProperty("major", "0")),
                Integer.parseInt(props.getProperty("minor", "0")),
                Integer.parseInt(props.getProperty("patch", "0"))
            };
        }
    }

    public static int[] loadVersion(URL url) throws IOException {
        Properties props = new Properties();
        try (InputStream input = url.openStream()) {
            props.load(input);
            return new int[]{
                Integer.parseInt(props.getProperty("major", "0")),
                Integer.parseInt(props.getProperty("minor", "0")),
                Integer.parseInt(props.getProperty("patch", "0"))
            };
        }
    }

    public static boolean isNewer(int[] local, int[] remote) {
        for (int i = 0; i < 3; i++) {
            if (remote[i] > local[i]) return true;
            if (remote[i] < local[i]) return false;
        }
        return false;
    }

    public static String versionString(int[] v) {
        return v[0] + "." + v[1] + "." + v[2];
    }
    
    public static int[] parseVersionString(String version) {
        String[] parts = version.split("\\.");
        int[] parsed = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            parsed[i] = Integer.parseInt(parts[i]);
        }
        return parsed;
    }
}