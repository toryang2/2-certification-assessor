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
public class DatabaseReportTypeHelper {
    private static final Logger logger = AdvancedLogger.getLogger(DatabaseReportTypeHelper.class.getName());
    private static final String TABLE_NAME = "report_type";
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            ConfigHelper.getDbUrl(),
            ConfigHelper.getDbUser(),
            ConfigHelper.getDbPassword());
    }
    
    public static String generateObjId(String reportType) {
        char firstLetter = reportType.trim().toUpperCase().charAt(0);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(reportType.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long crcValue = crc.getValue();
        return firstLetter + "-" + Long.toHexString(crcValue) + ":";
    }
    
    private static String toTitleCase(String input) {
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1));
                result.append(" ");
            }
        }
        return result.toString().trim();
    }
    
    public static void insertReportTypeToDatabase(String reportType) throws Exception {
        String formattedType = toTitleCase(reportType);
        String objId = generateObjId(formattedType);
        String sql = "INSERT INTO report_type (objid, type) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, objId);
            stmt.setString(2, formattedType);
            stmt.executeUpdate();
            logger.log(Level.INFO, "Inserted report type into database: {0}", formattedType);
        }
    }

    public static String fetchObjIdPrefix(Connection conn, String reportType) throws SQLException {
        String sql = "SELECT objid FROM report_type WHERE type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("objid");
                } else {
                    throw new SQLException("No objid found for report type: " + reportType);
                }
            }
        }
    }
}
