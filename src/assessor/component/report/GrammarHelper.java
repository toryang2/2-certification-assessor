package assessor.component.report;

public class GrammarHelper {
    public static String getPossessive(String parentStatus) {
        if (parentStatus == null) return "";

        return switch (parentStatus) {
            case "Male" -> "his";
            case "Female" -> "her";
            case "Married" -> "their";
            case "Live-in" -> "their";
            case "Guardian" -> "";
            default -> "";
        };
    }
}
