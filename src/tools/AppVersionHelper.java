/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class AppVersionHelper {

    private static String cachedVersion = null;

    public static String getAppVersion() {
        if (cachedVersion != null) return cachedVersion;

        try {
            Package pkg = AppVersionHelper.class.getPackage();
            if (pkg != null) {
                String version = pkg.getImplementationVersion();
                if (version != null) {
                    cachedVersion = version;
                    return version;
                }
            }

            // Fallback: read custom App-Version from manifest
            try (InputStream is = AppVersionHelper.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
                if (is != null) {
                    Manifest manifest = new Manifest(is);
                    Attributes attr = manifest.getMainAttributes();
                    cachedVersion = attr.getValue("App-Version");
                    if (cachedVersion != null) return cachedVersion;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cachedVersion = "1.0.0";
        return cachedVersion;
    }
}