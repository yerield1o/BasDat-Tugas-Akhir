package controller;

import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController {

    public String[] getUserData(String username) {
        String loadQuery = "SELECT nama_pelanggan, Username, Password, no_telepon, alamat_lengkap FROM Pelanggan WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(loadQuery)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                        rs.getString("nama_pelanggan"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("no_telepon"),
                        rs.getString("alamat_lengkap")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUserProfile(String username, String name, String password, String phone, String address) {
        String updateQuery = "UPDATE Pelanggan SET nama_pelanggan = ?, Password = ?, no_telepon = ?, alamat_lengkap = ? WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setString(5, username);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}