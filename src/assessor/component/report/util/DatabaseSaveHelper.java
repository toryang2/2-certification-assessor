package assessor.component.report.util;

import assessor.utils.AdvancedLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseSaveHelper {
    private static final Logger logger = AdvancedLogger.getLogger(DatabaseSaveHelper.class.getName());
    private static final String TABLE_NAME = "reports";

    private static final Map<Class<?>, Integer> SQL_TYPES = new HashMap<>();
    static {
        SQL_TYPES.put(String.class, Types.VARCHAR);
        SQL_TYPES.put(Integer.class, Types.INTEGER);
        SQL_TYPES.put(Long.class, Types.BIGINT);
        SQL_TYPES.put(Double.class, Types.DECIMAL);
        SQL_TYPES.put(LocalDate.class, Types.DATE);
        SQL_TYPES.put(LocalTime.class, Types.TIME);
    }

    public static String getAssessorName(int id) {
        String sql = "SELECT Assessor FROM sys_signatories WHERE id = ?";
        logger.log(Level.FINE, "Querying assessor name with ID: {0}", id);

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String assessor = rs.getString("Assessor");
                    logger.log(Level.INFO, "Assessor found: {0}", assessor);
                    return assessor;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve assessor for ID: " + id, e);
        }
        return null;
    }
    
    public static int saveReportAndGetNewestId(String reportType, Map<String, Object> data) {
        logger.log(Level.INFO, "Saving report of type: {0} with data: {1}", new Object[]{reportType, data});

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword())) {

            data.put("Type", reportType);
            String sql = buildInsertQuery(data.keySet());

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Report saved successfully: {0}", data);

                // Notify listeners about the data change
                DataChangeNotifier.getInstance().notifyDataChange();

                // Retrieve and return the newest record ID
                int newestId = getNewestRecordId(reportType);
                logger.log(Level.INFO, "Newest record ID after save: {0}", newestId);
                return newestId;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save report: " + reportType, e);
            return -1; // Indicate failure
        }
    }

    public static boolean saveReport(String reportType, Map<String, Object> data) {
        logger.log(Level.INFO, "Saving report of type: {0} with data: {1}", new Object[]{reportType, data});

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword())) {

            data.put("Type", reportType);
            String sql = buildInsertQuery(data.keySet());

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Report saved successfully: {0}", data);
                
                // Notify listeners about the data change
                DataChangeNotifier.getInstance().notifyDataChange();
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save report: " + reportType, e);
            return false;
        }
    }

    public static int getNewestRecordId(String reportType) {
        String sql = "SELECT MAX(id) AS newestId FROM " + TABLE_NAME + " WHERE Type = ?";
        logger.log(Level.FINE, "Querying newest record ID for report type: {0}", reportType);

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reportType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newestId = rs.getInt("newestId");
                    logger.log(Level.INFO, "Newest record ID found: {0}", newestId);
                    return newestId;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get newest record ID for report type: " + reportType, e);
        }
        return -1; // Return -1 if no record found
    }

    private static String buildInsertQuery(java.util.Set<String> columns) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();

        for (String column : columns) {
            columnsPart.append(column).append(",");
            valuesPart.append("?,");

        }

        if (columnsPart.length() > 0) {
            columnsPart.setLength(columnsPart.length() - 1);
            valuesPart.setLength(valuesPart.length() - 1);
        }

        String query = "INSERT INTO " + TABLE_NAME + " (" + columnsPart + ") " + "VALUES (" + valuesPart + ")";
        logger.log(Level.FINE, "Generated SQL query: {0}", query);
        return query;
    }

    private static void setParameters(PreparedStatement pstmt, Map<String, Object> data) throws SQLException {
        int index = 1;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            logger.log(Level.FINE, "Setting parameter: {0} to value: {1}", new Object[]{entry.getKey(), value});

            if (value == null) {
                // Handle null values explicitly
                pstmt.setNull(index, Types.NULL);
            } else if (value instanceof LocalDate) {
                pstmt.setDate(index, Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalTime) {
                pstmt.setTime(index, Time.valueOf((LocalTime) value));
            } else if (SQL_TYPES.containsKey(value.getClass())) {
                pstmt.setObject(index, value, SQL_TYPES.get(value.getClass()));
            } else {
                throw new SQLException("Unsupported data type: " + value.getClass());
            }
            index++;
        }
    }
}