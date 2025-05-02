package assessor.component.report.util;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseSaveHelper {
    private static final Logger logger = Logger.getLogger(DatabaseSaveHelper.class.getName());
    private static final String TABLE_NAME = "reports";
    
    // Map Java types to SQL type constants
    private static final Map<Class<?>, Integer> SQL_TYPES = new HashMap<>();
    static {
        SQL_TYPES.put(String.class, Types.VARCHAR);
        SQL_TYPES.put(Integer.class, Types.INTEGER);
        SQL_TYPES.put(Long.class, Types.BIGINT);  // Add this line
        SQL_TYPES.put(Double.class, Types.DECIMAL);
        SQL_TYPES.put(LocalDate.class, Types.DATE);
        SQL_TYPES.put(LocalTime.class, Types.TIME);
    }
    public static String getAssessorName(int id) {
        String sql = "SELECT Assessor FROM sys_signatories WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Assessor");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve assessor", e);
        }
        return null;
    }
    public static boolean saveReport(String reportType, Map<String, Object> data) {
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword())) {
            
            // Add report type to data
            data.put("Type", reportType);
            
            String sql = buildInsertQuery(data.keySet());
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save {0} report: {1}", 
                new Object[]{reportType, e.getMessage()});
            return false;
        }
    }
    
    public static int getNewestRecordId(String reportType) throws SQLException {
        String sql = "SELECT MAX(id) AS newestId FROM " + TABLE_NAME + " WHERE Type = ?";
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reportType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("newestId");
                } else {
                    throw new SQLException("No records found for report type: " + reportType);
                }
            }
        }
    }

    private static String buildInsertQuery(java.util.Set<String> columns) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        
        for (String column : columns) {
            columnsPart.append(column).append(",");
            valuesPart.append("?,");
        }
        
        // Remove trailing commas
        if (columnsPart.length() > 0) {
            columnsPart.setLength(columnsPart.length() - 1);
            valuesPart.setLength(valuesPart.length() - 1);
        }
        
        return "INSERT INTO " + TABLE_NAME + " (" + columnsPart + ") " +
               "VALUES (" + valuesPart + ")";
    }

    private static void setParameters(PreparedStatement pstmt, Map<String, Object> data) 
        throws SQLException {
        
        int index = 1;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            Class<?> valueType = value.getClass();
            
            // Handle special types
            if (value instanceof LocalDate) {
                pstmt.setDate(index, Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalTime) {
                pstmt.setTime(index, Time.valueOf((LocalTime) value));
            } else {
                // Use generic type mapping
                pstmt.setObject(index, value, SQL_TYPES.get(valueType));
            }
            
            index++;
        }
    }
}
