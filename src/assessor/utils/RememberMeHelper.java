/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.utils;

import java.io.*;
import java.util.Properties;

/**
 *
 * @author user
 */
public class RememberMeHelper {
    private static final String CONFIG_FILE = "config.cfg";

    public static void saveUsername(String username) {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                file.createNewFile(); // ✅ Auto-create file
            }

            Properties props = new Properties();
            props.setProperty("username", username);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Remember Me Config");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String loadUsername() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            return props.getProperty("username", "");
        } catch (IOException ex) {
            return "";
        }
    }
    
    public static void clear() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
