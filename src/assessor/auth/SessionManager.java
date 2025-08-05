package assessor.auth;

public class SessionManager {
    private static SessionManager instance;
    private String loggedInUsername;
    private String userInitials;
    private String userFullName;
    private int accessLevel = -1; // 0 = user, 1 = admin

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
    
    // Getter and Setter for userInitials
    public String getFullName() {
        return userFullName;
    }

    public void setFullName(String name) {
        this.userFullName = name;
    }
    
    public int getAccessLevel() {
        return accessLevel;
    }
    
    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    public boolean isAdmin() {
        return accessLevel == 1;
    }

    // Clear session data (useful for logout)
    public void clearSession() {
        this.loggedInUsername = null;
        this.userInitials = null;
        this.accessLevel = -1;
    }
}