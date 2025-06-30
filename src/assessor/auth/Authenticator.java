package assessor.auth;

import assessor.component.report.util.ConfigHelper;

import java.security.*;
import java.sql.*;

public class Authenticator {

    public static boolean authenticate(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            throw new RuntimeException("Error hashing the password");
        }

        try (Connection connection = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword())) {

            String query = "SELECT initial, accesslevel FROM sys_login_credentials WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, hashedPassword);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Store the username and initials in SessionManager
                        SessionManager.getInstance().setLoggedInUsername(username);
                        SessionManager.getInstance().setUserInitials(resultSet.getString("initial"));
                        SessionManager.getInstance().setAccessLevel(resultSet.getInt("accesslevel"));

                        // Debugging: Print stored values
                        System.out.println("Session - Username: " + SessionManager.getInstance().getLoggedInUsername());
                        System.out.println("Session - User Initials: " + SessionManager.getInstance().getUserInitials());
                        System.out.println("Session - Access Level: " + SessionManager.getInstance().getAccessLevel());

                        return true; // Authentication successful
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error authenticating user: " + ex.getMessage());
        }
        return false; // Authentication failed
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static boolean register(String username, String password, String name, String initial) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            throw new RuntimeException("Error hashing the password");
        }

        try (Connection connection = DriverManager.getConnection(
                ConfigHelper.getDbUrl(),
                ConfigHelper.getDbUser(),
                ConfigHelper.getDbPassword())) {

            // Check if user already exists
            String queryCheck = "SELECT 1 FROM sys_login_credentials WHERE username = ?";
            try (PreparedStatement preparedStatementCheck = connection.prepareStatement(queryCheck)) {
                preparedStatementCheck.setString(1, username);

                try (ResultSet resultSet = preparedStatementCheck.executeQuery()) {
                    if (resultSet.next()) {
                        // User already exists
                        return false;
                    }
                }
            }

            // Insert new user
            int accesslevel = 0; // or whatever default you want
            String queryInsert = "INSERT INTO sys_login_credentials (username, password, initial, accesslevel, name) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(queryInsert)) {
                preparedStatementInsert.setString(1, username);
                preparedStatementInsert.setString(2, hashedPassword);
                preparedStatementInsert.setString(3, initial); // Use supplied initial
                preparedStatementInsert.setInt(4, accesslevel);
                preparedStatementInsert.setString(5, name);

                int rows = preparedStatementInsert.executeUpdate();
                return rows > 0;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error registering user: " + ex.getMessage());
        }
    }
}