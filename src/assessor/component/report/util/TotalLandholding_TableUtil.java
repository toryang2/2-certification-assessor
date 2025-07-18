/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author user
 */
public class TotalLandholding_TableUtil {
    public static List<String> getColumnNames(String tableName) throws Exception {
        List<String> columns = new ArrayList<>();
        try (
            Connection conn = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword()
            );
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tableName + " LIMIT 1");
            ResultSet rs = ps.executeQuery();
        ) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columns.add(meta.getColumnName(i));
            }
        }
        return columns;
    }
}
