package assessor.auth;

import assessor.component.report.util.ConfigHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

            String query = "SELECT COUNT(*) FROM sys_login_credentials WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, hashedPassword);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        return true; // User authenticated
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
}