package assessor.component.report.util;

import assessor.utils.AdvancedLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseTotalLandholdingHelper {
    private static final Logger logger = AdvancedLogger.getLogger(DatabaseTotalLandholdingHelper.class.getName());
    private static final String TABLE_NAME = "reports_total_landholding";

    private static final Map<Class<?>, Integer> SQL_TYPES = new HashMap<>();
    static {
        SQL_TYPES.put(String.class, Types.VARCHAR);
        SQL_TYPES.put(Integer.class, Types.INTEGER);
        SQL_TYPES.put(Long.class, Types.BIGINT);
        SQL_TYPES.put(Double.class, Types.DECIMAL);
        SQL_TYPES.put(LocalDate.class, Types.DATE);
        SQL_TYPES.put(LocalTime.class, Types.TIME);
    }

    // Cache for objid prefixes per report type
    private static final Map<String, String> objIdPrefixCache = new HashMap<>();

    /**
     * Generates an objid like: H-40cf0891
     * @param conn Active DB connection
     * @param reportType The report type name (e.g., "Hospitalization")
     * @return Generated objid string
     * @throws SQLException If query fails or prefix not found
     */

    public static List<String> fetchBarangays() {
        List<String> barangays = new ArrayList<>();
        String sql = "SELECT barangay_name FROM sys_barangay_list";
        logger.log(Level.FINE, "Executing query to fetch barangays: {0}", sql);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                barangays.add(rs.getString("barangay_name"));
            }
            logger.log(Level.INFO, "Barangays fetched successfully: {0}", barangays);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch barangays", e);
        }
        return barangays;
    }

    public static List<String> fetchTypeSet() {
        List<String> typesets = new ArrayList<>();
        String sql = "SELECT type FROM report_type";
        logger.log(Level.FINE, "Executing query to fetch typesets: {0}", sql);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                typesets.add(rs.getString("type"));
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

        try (Connection conn = getConnection();
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
        try (Connection conn = getConnection();
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
        try (Connection conn = getConnection()) {
            data.put("Type", reportType);
            data.put("objid", generateFullObjId(conn, reportType)); // Add objid before saving

            String sql = buildInsertQuery(data.keySet());
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Report saved successfully: {0}", data);
                DataChangeNotifier.getInstance().notifyDataChange();
                int newestId = getNewestRecordId(reportType);
                logger.log(Level.INFO, "Newest record ID after save: {0}", newestId);
                return newestId;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save report: " + reportType, e);
            return -1;
        }
    }

    public static boolean saveReport(String reportType, Map<String, Object> data) {
        logger.log(Level.INFO, "Saving report of type: {0} with data: {1}", new Object[]{reportType, data});
        try (Connection conn = getConnection()) {
            data.put("Type", reportType);
            data.put("objid", generateFullObjId(conn, reportType)); // Add objid before saving

            String sql = buildInsertQuery(data.keySet());
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Report saved successfully: {0}", data);
                DataChangeNotifier.getInstance().notifyDataChange();
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save report: " + reportType, e);
            return false;
        }
    }

    public static boolean saveReport(String reportType, Map<String, Object> data, String objid) {
        logger.log(Level.INFO, "Saving report of type: {0} with data: {1} and objid: {2}", new Object[]{reportType, data, objid});
        try (Connection conn = getConnection()) {
            data.put("Type", reportType);
            data.put("objid", objid); // Use the provided objid

            String sql = buildInsertQuery(data.keySet());
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Report saved successfully: {0}", data);
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
        try (Connection conn = getConnection();
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
        return -1;
    }

    private static String buildInsertQuery(Set<String> columns) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        for (String column : columns) {
            columnsPart.append(column).append(",");
            valuesPart.append("?,");
        }
        columnsPart.setLength(columnsPart.length() - 1);
        valuesPart.setLength(valuesPart.length() - 1);
        String query = "INSERT INTO " + TABLE_NAME + " (" + columnsPart + ") VALUES (" + valuesPart + ")";
        logger.log(Level.FINE, "Generated SQL query: {0}", query);
        return query;
    }

    // Update setParameters to handle different data types
    private static void setParameters(PreparedStatement pstmt, Map<String, Object> data) throws SQLException {
        int index = 1;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                pstmt.setNull(index, Types.NULL);
            } else if (value instanceof LocalDate) {
                pstmt.setDate(index, Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalTime) {
                pstmt.setTime(index, Time.valueOf((LocalTime) value));
            } else if (value instanceof Double) {
                pstmt.setDouble(index, (Double) value);
            } else if (value instanceof Integer) {
                pstmt.setInt(index, (Integer) value);
            } else if (value instanceof Long) {
                pstmt.setLong(index, (Long) value);
            } else {
                pstmt.setString(index, value.toString());
            }
            index++;
        }
    }

    public static String generateFullObjId(Connection conn, String reportType) throws SQLException {
        String prefix = getObjIdPrefixCached(conn, reportType);
        String randomPart = UUID.randomUUID().toString().substring(0, 4);
        return prefix + "-" + randomPart;
    }

    private static String getObjIdPrefixCached(Connection conn, String reportType) throws SQLException {
        return objIdPrefixCache.computeIfAbsent(reportType, key -> {
            try {
                return DatabaseReportTypeHelper.fetchObjIdPrefix(conn, key);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean updateReportField(int id, String column, Object value) {
        String sql = "UPDATE " + TABLE_NAME + " SET " + column + " = ? WHERE id = ?";
        logger.log(Level.FINE, "Updating report field: id={0}, column={1}, value={2}", new Object[]{id, column, value});
        try (Connection conn = getConnection();
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

    public static List<String> getAutocompleteSuggestions(String fieldKey, String prefix) {
        List<String> suggestions = new ArrayList<>();
        String sql = "SELECT value FROM autocomplete_cache WHERE field_key = ? AND LOWER(value) LIKE LOWER(CONCAT('%', ?, '%')) ORDER BY value LIMIT 10";
        try (Connection conn = getConnection();
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
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fieldKey);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error saving autocomplete value", e);
        }
    }

    public static boolean saveSubReport(String reportType, String tableName, Map<String, Object> data) {
        logger.log(Level.INFO, "Saving sub-report to table: {0} with data: {1}", 
            new Object[]{tableName, data});

        try (Connection conn = getConnection()) {
            data.put("objid", generateFullObjId(conn, reportType));
            // Use the same insert logic as saveReport but for sub-tables
            String sql = buildInsertQuery(tableName, data.keySet());
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, data);
                pstmt.executeUpdate();
                logger.log(Level.INFO, "Sub-report saved successfully");
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save sub-report to table: " + tableName, e);
            return false;
        }
    }

    // Modify buildInsertQuery to accept table name
    private static String buildInsertQuery(String tableName, Set<String> columns) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        for (String column : columns) {
            columnsPart.append(column).append(",");
            valuesPart.append("?,");
        }
        columnsPart.setLength(columnsPart.length() - 1);
        valuesPart.setLength(valuesPart.length() - 1);
        return "INSERT INTO " + tableName + " (" + columnsPart + ") VALUES (" + valuesPart + ")";
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            ConfigHelper.getDbUrl(),
            ConfigHelper.getDbUser(),
            ConfigHelper.getDbPassword());
    }
    
    // Method to ensure the report type exists in the database
    public static void ensureReportTypeExists() {
        try (Connection conn = getConnection()) {
            // Check if "Total Landholding" exists in report_type table
            String checkSql = "SELECT COUNT(*) FROM report_type WHERE type = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, "Total Landholding");
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Report type doesn't exist, add it
                        logger.log(Level.INFO, "Adding 'Total Landholding' report type to database");
                        DatabaseReportTypeHelper.insertReportTypeToDatabase("Total Landholding");
                        logger.log(Level.INFO, "Successfully added 'Total Landholding' report type");
                    } else {
                        logger.log(Level.INFO, "'Total Landholding' report type already exists");
                    }
                }
            }
            
            // Ensure foreign key relationship exists
            setupForeignKeyRelationship(conn);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not ensure report type exists: " + e.getMessage(), e);
        }
    }
    
    // Set up foreign key relationship between reports_total_landholding and report_sub_total_landholding
    private static void setupForeignKeyRelationship(Connection conn) {
        try {
            // Check if foreign key already exists
            String checkFkSql = "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE " +
                               "WHERE TABLE_SCHEMA = DATABASE() " +
                               "AND TABLE_NAME = 'report_sub_total_landholding' " +
                               "AND COLUMN_NAME = 'rtlid' " +
                               "AND REFERENCED_TABLE_NAME = 'reports_total_landholding'";
            
            try (PreparedStatement checkFkStmt = conn.prepareStatement(checkFkSql)) {
                try (ResultSet rs = checkFkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Foreign key doesn't exist, try to create it
                        logger.log(Level.INFO, "Attempting to set up foreign key relationship...");
                        
                        try {
                            // First, clean up any orphaned records that would violate the constraint
                            cleanupOrphanedRecords(conn);
                            
                            // Then try to create the foreign key constraint
                            String addFkSql = "ALTER TABLE report_sub_total_landholding " +
                                            "ADD CONSTRAINT fk_rtlid_objid " +
                                            "FOREIGN KEY (rtlid) REFERENCES reports_total_landholding(objid) " +
                                            "ON DELETE CASCADE ON UPDATE CASCADE";
                            
                            try (PreparedStatement addFkStmt = conn.prepareStatement(addFkSql)) {
                                addFkStmt.executeUpdate();
                                logger.log(Level.INFO, "Foreign key relationship created successfully");
                            }
                        } catch (SQLException fkError) {
                            // If foreign key creation fails, log the manual setup requirement
                            logger.log(Level.WARNING, "Could not create foreign key constraint automatically. " +
                                "Please manually clean up orphaned records and create the constraint: " +
                                "DELETE FROM report_sub_total_landholding WHERE rtlid NOT IN (SELECT objid FROM reports_total_landholding); " +
                                "CREATE INDEX idx_objid ON reports_total_landholding(objid); " +
                                "ALTER TABLE report_sub_total_landholding ADD CONSTRAINT fk_rtlid_objid " +
                                "FOREIGN KEY (rtlid) REFERENCES reports_total_landholding(objid) " +
                                "ON DELETE CASCADE ON UPDATE CASCADE;", fkError);
                        }
                    } else {
                        logger.log(Level.INFO, "Foreign key relationship already exists");
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not check foreign key relationship: " + e.getMessage(), e);
        }
    }
    
    // Clean up orphaned records that would violate foreign key constraint
    private static void cleanupOrphanedRecords(Connection conn) {
        try {
            String cleanupSql = "DELETE FROM report_sub_total_landholding " +
                               "WHERE rtlid NOT IN (SELECT objid FROM reports_total_landholding) " +
                               "OR rtlid IS NULL";
            
            try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupSql)) {
                int deletedRows = cleanupStmt.executeUpdate();
                if (deletedRows > 0) {
                    logger.log(Level.INFO, "Cleaned up {0} orphaned records from report_sub_total_landholding", deletedRows);
                } else {
                    logger.log(Level.INFO, "No orphaned records found to clean up");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not clean up orphaned records: " + e.getMessage(), e);
        }
    }
    
    // Get the last generated objid from the main report
    public static String getLastGeneratedObjid() {
        String sql = "SELECT objid FROM reports_total_landholding WHERE Type = 'Total Landholding' ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("objid");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve last generated objid", e);
        }
        return null;
    }
    
    // Save table data to report_sub_total_landholding using foreign key
    public static int saveTableDataWithForeignKey(String parentObjid, List<Map<String, Object>> tableData) {
        int savedRows = 0;

        for (Map<String, Object> rowData : tableData) {
            // Check if row has any data (excluding rtlid)
            boolean hasData = false;
            for (String key : rowData.keySet()) {
                if (key.equalsIgnoreCase("rtlid")) continue;
                Object value = rowData.get(key);
                if (value != null && !value.toString().isEmpty()) {
                    hasData = true;
                    break;
                }
            }

            if (hasData) {
                boolean success = saveSubReport("Total Landholding", "report_sub_total_landholding", rowData);
                if (success) {
                    savedRows++;
                }
            }
        }
        logger.log(Level.INFO, "Saved {0} table rows with foreign key {1}", new Object[]{savedRows, parentObjid});
        return savedRows;
    }
}