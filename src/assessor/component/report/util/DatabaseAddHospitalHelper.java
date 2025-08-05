/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import assessor.utils.AdvancedLogger;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class DatabaseAddHospitalHelper {
    private static final Logger logger = AdvancedLogger.getLogger(DatabaseAddHospitalHelper.class.getName());
    private static final String TABLE_NAME = "autocomplete_cache";
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            ConfigHelper.getDbUrl(),
            ConfigHelper.getDbUser(),
            ConfigHelper.getDbPassword());
    }
    
    public static String generateObjId(String addHospital) {
        char firstLetter = addHospital.trim().toUpperCase().charAt(0);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(addHospital.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long crcValue = crc.getValue();
        return firstLetter + "-" + Long.toHexString(crcValue) + ":";
    }
    
//    private static String toTitleCase(String input) {;
//        String[] words = input.trim().toLowerCase().split("\\s+");
//        StringBuilder result = new StringBuilder();
//        for (String word : words) {
//            if (!word.isEmpty()) {
//                result.append(Character.toUpperCase(word.charAt(0)))
//                      .append(word.substring(1));
//                result.append(" ");
//            }
//        }
//        return result.toString().trim();
//    }
    
    public static void insertHospitalNameToDatabase(String addHospital) throws Exception {
        String formattedType = addHospital;
        String objId = generateObjId(formattedType);
        String sql = "INSERT INTO "+ TABLE_NAME +" (field_key, value) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "txtHospital");
            stmt.setString(2, formattedType);
            stmt.executeUpdate();
            logger.log(Level.INFO, "Inserted new hospital name into database: {0}", formattedType);
        }
    }
    
    public static void insertHospitalAddressToDatabase(String addHospital) throws Exception {
        String formattedType = addHospital;
        String objId = generateObjId(formattedType);
        String sql = "INSERT INTO "+ TABLE_NAME +" (field_key, value) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "txtHospitalAddress");
            stmt.setString(2, formattedType);
            stmt.executeUpdate();
            logger.log(Level.INFO, "Inserted new hospital name into database: {0}", formattedType);
        }
    }

    public static String fetchObjIdPrefix(Connection conn, String addHospital) throws SQLException {
        String sql = "SELECT objid FROM report_type WHERE type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, addHospital);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("objid");
                } else {
                    throw new SQLException("No objid found for report type: " + addHospital);
                }
            }
        }
    }
}
