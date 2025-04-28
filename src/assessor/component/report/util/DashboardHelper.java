/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import assessor.component.report.util.ConfigHelper;
import java.sql.*;
import java.util.logging.*;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;

public class DashboardHelper {
    private static final Logger logger = Logger.getLogger(DashboardHelper.class.getName());

    public static int getTotalRows() {
        final String SQL = "SELECT COUNT(*) AS total FROM reports";
        
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(), 
                ConfigHelper.getDbUser(), 
                ConfigHelper.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error getting total rows", ex);
            return 0;
        }
    }

    public static int getTodayClientsServed() {
        final String SQL = "SELECT COUNT(*) AS today_total FROM reports "
                          + "WHERE DATE(CertificationDate) = CURDATE()";
                          
        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            
            return rs.next() ? rs.getInt("today_total") : 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error getting today's registrations", ex);
            return 0;
        }
    }
    
    public static int getYesterdayClientsServed() {
        final String SQL = "SELECT COUNT(*) AS yesterday_total FROM reports "
                          + "WHERE DATE(CertificationDate) = CURDATE() - INTERVAL 1 DAY";

        try (Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            return rs.next() ? rs.getInt("yesterday_total") : 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error getting yesterday's registrations", ex);
            return 0;
        }
    }
    
public static TimeTableXYDataset getDailyClientsDataset() {
    TimeTableXYDataset dataset = new TimeTableXYDataset();
    String seriesName = "Clients Served";
    final String SQL = "SELECT CertificationDate, CertificationTime, COUNT(*) AS count " +
                       "FROM reports " +
                       "GROUP BY CertificationDate, CertificationTime " +
                       "ORDER BY CertificationDate, CertificationTime";

        try (Connection conn = DriverManager.getConnection(
                 ConfigHelper.getDbUrl(), 
                 ConfigHelper.getDbUser(), 
                 ConfigHelper.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
            java.sql.Date certDate = rs.getDate("CertificationDate");
            java.sql.Time certTime = rs.getTime("CertificationTime");
            if (certDate != null && certTime != null) {
                // Combine date and time
                long datetime = certDate.getTime() + certTime.getTime();
                Second period = new Second(new Date(datetime)); // Use Second for time precision
                dataset.add(period, rs.getInt("count"), seriesName);
            }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching daily clients data", ex);
        }
        return dataset;
    }
}