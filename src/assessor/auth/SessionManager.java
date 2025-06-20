package assessor.auth;

public class SessionManager {
    private static SessionManager instance;
    private String loggedInUsername;
    private String userInitials;

    // Private constructor to prevent direct instantiation
    private SessionManager() {}

    // Singleton pattern to ensure only one instance
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getter and Setter for loggedInUsername
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    // Getter and Setter for userInitials
    public String getUserInitials() {
        return userInitials;
    }

    public void setUserInitials(String initials) {
        this.userInitials = initials;
    }

    // Clear session data (useful for logout)
    public void clearSession() {
        this.loggedInUsername = null;
        this.userInitials = null;
    }
}