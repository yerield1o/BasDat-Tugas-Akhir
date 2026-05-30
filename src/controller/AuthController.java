package controller;

import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {

    public boolean authenticateUser(String enteredUsername, String enteredPassword) {
        String sqlQuery = "SELECT * FROM Pelanggan WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, enteredUsername);
            pstmt.setString(2, enteredPassword);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            System.out.println("Database connection error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerNewUser(String username, String rawPassword) {
        if (username.isEmpty() || rawPassword.isEmpty()) return false;

        String sqlQuery = "INSERT INTO Pelanggan (Username, Password, nama_pelanggan, alamat_lengkap) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, username);
            pstmt.setString(2, rawPassword);
            pstmt.setString(3, "New Customer");
            pstmt.setString(4, "Please update your address in Account Settings");

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}