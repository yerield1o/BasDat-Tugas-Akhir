package controller;

import model.CartItem;
import model.Courier;
import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CartController {

    public List<Courier> getCouriers() {
        List<Courier> couriers = new ArrayList<>();
        String query = "SELECT id_pengirim, nama_ekspedisi FROM Pengirim";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                couriers.add(new Courier(rs.getInt("id_pengirim"), rs.getString("nama_ekspedisi")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return couriers;
    }

    public boolean processCheckoutTransaction(String loggedInUser, double totalHarga, int courierId, String paymentMethod, List<CartItem> cartItems) {
        String getCustomerSql = "SELECT id_pelanggan FROM Pelanggan WHERE Username = ?";
        String insertPesananSql = "INSERT INTO Pesanan (id_pelanggan, tanggal_pesanan, status_pesanan, total_harga) VALUES (?, GETDATE(), ?, ?)";
        String insertDetailSql = "INSERT INTO Produk_Dibeli (id_pesanan, id_varian, kuantitas, harga_satuan) VALUES (?, ?, ?, ?)";
        String insertPembayaranSql = "INSERT INTO Pembayaran (id_pesanan, tanggal_bayar, metode_pembayaran, jumlah_bayar, status_pembayaran) VALUES (?, GETDATE(), ?, ?, ?)";
        String updateStockSql = "UPDATE Produk_Varian SET stok = stok - ? WHERE id_varian = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = -1;
            try (PreparedStatement pstmtCustomer = conn.prepareStatement(getCustomerSql)) {
                pstmtCustomer.setString(1, loggedInUser);
                ResultSet rsCustomer = pstmtCustomer.executeQuery();
                if (rsCustomer.next()) {
                    customerId = rsCustomer.getInt("id_pelanggan");
                } else {
                    return false;
                }
            }

            conn.setAutoCommit(false);
            try {
                int newPesananId = -1;
                try (PreparedStatement pstmtPesanan = conn.prepareStatement(insertPesananSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtPesanan.setInt(1, customerId);
                    pstmtPesanan.setString(2, "Pending");
                    pstmtPesanan.setDouble(3, totalHarga);
                    pstmtPesanan.executeUpdate();

                    ResultSet generatedKeys = pstmtPesanan.getGeneratedKeys();
                    if (generatedKeys.next()) newPesananId = generatedKeys.getInt(1);
                }

                try (PreparedStatement pstmtDetail = conn.prepareStatement(insertDetailSql);
                     PreparedStatement pstmtStock = conn.prepareStatement(updateStockSql)) {
                    for (CartItem item : cartItems) {
                        pstmtDetail.setInt(1, newPesananId);
                        pstmtDetail.setInt(2, item.getVariantId());
                        pstmtDetail.setInt(3, item.getQuantity());
                        pstmtDetail.setDouble(4, item.getPrice());
                        pstmtDetail.addBatch();

                        pstmtStock.setInt(1, item.getQuantity());
                        pstmtStock.setInt(2, item.getVariantId());
                        pstmtStock.addBatch();
                    }
                    pstmtDetail.executeBatch();
                    pstmtStock.executeBatch();
                }

                try (PreparedStatement pstmtPembayaran = conn.prepareStatement(insertPembayaranSql)) {
                    pstmtPembayaran.setInt(1, newPesananId);
                    pstmtPembayaran.setString(2, paymentMethod);
                    pstmtPembayaran.setDouble(3, totalHarga);
                    pstmtPembayaran.setString(4, "Berhasil");
                    pstmtPembayaran.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}