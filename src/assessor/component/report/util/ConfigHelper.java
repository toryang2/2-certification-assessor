package assessor.component.report.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class ConfigHelper {
    private static final Logger logger = Logger.getLogger(ConfigHelper.class.getName());
    private static final Map<String, String> configMap = new HashMap<>();
    
    static {
        loadConfig(); // Load the config once during class initialization
    }
    
    private static void loadConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader("config.ini"))) {
            String line;
            boolean inDatabaseSection = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                
                if (line.equalsIgnoreCase("[Database]")) {
                    inDatabaseSection = true;
                    continue;
                }
                
                if (inDatabaseSection) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim().replaceAll(";$", "");
                        configMap.put(key, value);
                    }
                }
            }
        } catch (IOException ex) {
            logger.severe("Error reading configuration: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static String getDbUrl() {
        if (!validateConfigKeys()) {
            logger.severe("Invalid/missing configuration values");
            throw new RuntimeException("Configuration error");
        }

        return String.format("jdbc:mysql://%s:%s/%s",
            configMap.get("Server"),
            configMap.get("Port"),
            configMap.get("Database"));
    }
   
    public static String getDbUser() {
        return configMap.get("User ID");
    }
    
    public static String getDbPassword() {
        return configMap.getOrDefault("Password", "");
    }

    private static boolean validateConfigKeys() {
        String[] requiredKeys = {"Server", "Port", "Database", "User ID", "Password"};
        List<String> missingKeys = Arrays.stream(requiredKeys)
            .filter(k -> !configMap.containsKey(k))
            .toList();
        
        if (!missingKeys.isEmpty()) {
            logger.log(Level.SEVERE, "Missing config keys: {0}", missingKeys);
            return false;
        }
        return true;
    }
}
