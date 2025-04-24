package assessor.component.report;

public class StatusUtil {
    public static String getParentStatus(String parentStatus) {
        if (parentStatus == null) return "";

        return switch (parentStatus) {
            case "MARRIED" -> "SPOUSES";
            case "SINGLE" -> "";
            default -> "";
        };
    }
}
