package assessor.component.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    // Main method for Date objects (SQL DATE type)
    public static String formatCertificationDate(Date date) {
        if (date == null) return "";

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int day = cal.get(Calendar.DAY_OF_MONTH);
        String month = new SimpleDateFormat("MMMM").format(date);
        int year = cal.get(Calendar.YEAR);

        // Use HTML <sup> tag with small font size for the suffix
        return String.format("%d<sup style='font-size:8px'>%s</sup> day of %s, %d",
                day,
                getOrdinalSuffix(day),
                month,
                year
        );
    }

    // Overloaded method for string dates (backward compatibility)
    public static String formatCertificationDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return "";

        SimpleDateFormat sdfParse = new SimpleDateFormat("yyyy-MM-dd"); // SQL date format
        sdfParse.setLenient(false);

        try {
            Date date = sdfParse.parse(dateString);
            return formatCertificationDate(date);
        } catch (ParseException e) {
            return "[INVALID DATE: " + dateString + "]";
        }
    }

    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) return "th";
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}