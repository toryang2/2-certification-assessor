package assessor.component.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Time;

public class DateFormatter {
    // Input format from SQL (mm-dd-yyyy)
    private static final SimpleDateFormat INPUT_FMT = new SimpleDateFormat("MM-dd-yyyy");
    // Output format (mm/dd/yyyy)
    private static final SimpleDateFormat OUTPUT_FMT = new SimpleDateFormat("MM/dd/yyyy");

    public static String formatCertificationDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }

        try {
            Date date = INPUT_FMT.parse(dateStr);
            return OUTPUT_FMT.format(date);
        } catch (ParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            return ""; // Return empty or raw value
        }
    }
    
    // Helper methods for date formatting
    public static String formatDate(Object date) {
        if (date instanceof Date) {
            return new SimpleDateFormat("MMM dd, yyyy").format((Date) date);
        }
        return String.valueOf(date);
    }

    public static String formatTime(Object time) {
        if (time instanceof Time) { // Now works with imported Time class
            return new SimpleDateFormat("hh:mm a").format((Time) time);
        }
        return String.valueOf(time);
    }
}
