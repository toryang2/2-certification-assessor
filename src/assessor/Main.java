/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package assessor;

import java.io.*;
import assessor.component.report.util.ConfigHelper;
import assessor.utils.DemoPreferences;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.util.FontUtils;
import java.awt.*;
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
        launchUI();
        
        System.out.println("Database URL: " + ConfigHelper.getDbUrl());
        System.out.println("User: " + ConfigHelper.getDbUser());
    }
    
    private static void createConfigIfMissing() {
        File configFile = new File("config.ini");
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(
                    "[Database]\n" +
                    "Server=localhost;\n" +
                    "Port=3306;\n" +
                    "Database=certificationdb;\n" +
                    "User ID=user;\n" +
                    "Password=;\n"
                );
                System.out.println("Created new config.ini with default values");
            } catch (Exception e) {
                System.err.println("Error creating config file:");
                e.printStackTrace();
            }
        }
    }
    
    private static void launchUI() {
        DemoPreferences.init();
//        DemoPreferences.clearPreferences();
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("assessor.themes");
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        DemoPreferences.setupLaf();
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
