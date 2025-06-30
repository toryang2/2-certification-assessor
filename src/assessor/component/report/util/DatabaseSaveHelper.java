package assessor.component.report.util;

import assessor.utils.AdvancedLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
    
    public static List<String> fetchBarangays() {
        List<String> barangays = new ArrayList<>();
        String sql = "SELECT barangay_name FROM sys_barangay_list";

        logger.log(Level.FINE, "Executing query to fetch barangays: {0}", sql);

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String barangay = rs.getString("barangay_name");
                barangays.add(barangay);
            }
            logger.log(Level.INFO, "Barangays fetched successfully: {0}", barangays);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch barangays", e);
        }

        return barangays;
    }
    
    public static List<String> fetchTypeSet() {
        List<String> typesets = new ArrayList<>();
        String sql = "SELECT typesets FROM sys_nolandholding_internaltypesets";

        logger.log(Level.FINE, "Executing query to fetch typesets: {0}", sql);

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String typeset = rs.getString("typesets");
                typesets.add(typeset);
            }
            logger.log(Level.INFO, "Typesets fetched successfully: {0}", typesets);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch typesets", e);
        }

        return typesets;
    }
    
    public static List<String> fetchRelationships(String maritalStatus, String gender) {
        List<String> relationships = new ArrayList<>();
        String sql = "SELECT Relationship FROM RelationshipMappings WHERE MaritalStatus = ?"
            + (gender != null ? " AND (Gender = ? OR Gender IS NULL)" : "");

        logger.log(Level.FINE, "Executing query to fetch relationships: {0}, maritalStatus={1}, gender={2}",
                new Object[]{sql, maritalStatus, gender});

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maritalStatus);
            if (gender != null) pstmt.setString(2, gender);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    relationships.add(rs.getString("Relationship"));
                }
                logger.log(Level.INFO, "Relationships fetched successfully: {0}", relationships);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch relationships", e);
        }

        return relationships;
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
    
    // Autocomplete Cache
    public static List<String> getAutocompleteSuggestions(String fieldKey, String prefix) {
        List<String> suggestions = new ArrayList<>();
        // Use case-insensitive matching
        String sql = "SELECT value FROM autocomplete_cache " +
                     "WHERE field_key = ? AND LOWER(value) LIKE LOWER(?) " +
                     "ORDER BY value LIMIT 10";

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fieldKey);
            pstmt.setString(2, prefix + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                suggestions.add(rs.getString("value"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get autocomplete suggestions", e);
        }
        return suggestions;
    }
    
    public static void saveAutocompleteValue(String fieldKey, String value) {
        if (value == null || value.isEmpty()) return;

        String sql = "INSERT IGNORE INTO autocomplete_cache (field_key, value) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fieldKey);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error saving autocomplete value", e);
        }
    }
    
    public static boolean updateReportField(int id, String column, Object value) {
        String sql = "UPDATE " + TABLE_NAME + " SET " + column + " = ? WHERE id = ?";
        logger.log(Level.FINE, "Updating report field: id={0}, column={1}, value={2}", new Object[]{id, column, value});
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            pstmt.setInt(2, id);
            int updated = pstmt.executeUpdate();
            logger.log(Level.INFO, "Update result: {0} row(s) updated", updated);
            return updated > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update report field", e);
            return false;
        }
    }
}