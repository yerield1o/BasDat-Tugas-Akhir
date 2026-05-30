package controller;

import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderController {

    //  riwayat pesanan berdasarkan username
    public List<String[]> getOrderHistory(String username) {
        List<String[]> orderList = new ArrayList<>();
        String query = "SELECT p.id_pesanan, p.tanggal_pesanan, p.status_pesanan, p.total_harga " +
                "FROM Pesanan p " +
                "JOIN Pelanggan cust ON p.id_pelanggan = cust.id_pelanggan " +
                "WHERE cust.Username = ? " +
                "ORDER BY p.tanggal_pesanan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                orderList.add(new String[]{
                        String.valueOf(rs.getInt("id_pesanan")),
                        rs.getString("tanggal_pesanan"),
                        rs.getString("status_pesanan"),
                        String.valueOf(rs.getDouble("total_harga"))
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderList;
    }

    // detail pengiriman / resi
    public String[] getDeliveryDetails(int orderId) {
        String query = "SELECT p.tanggal_kirim, p.nomor_resi, p.status_pengiriman, p.biaya_pengiriman, " +
                "e.nama_ekspedisi, e.nomor_telepon_pengirim " +
                "FROM Pengiriman p " +
                "JOIN Pengirim e ON p.id_pengirim = e.id_pengirim " +
                "WHERE p.id_pesanan = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new String[]{
                        rs.getString("nama_ekspedisi"),
                        rs.getString("nomor_telepon_pengirim"),
                        rs.getString("tanggal_kirim"),
                        rs.getString("nomor_resi"),
                        rs.getString("status_pengiriman"),
                        String.valueOf(rs.getDouble("biaya_pengiriman"))
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // return null jika resi belum dibuat oleh admin
    }

    // informasi Pembayaran
    public String[] getReceiptHeader(int orderId) {
        String payQuery = "SELECT id_pembayaran, tanggal_bayar, metode_pembayaran, status_pembayaran FROM Pembayaran WHERE id_pesanan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtPay = conn.prepareStatement(payQuery)) {

            pstmtPay.setInt(1, orderId);
            ResultSet rsPay = pstmtPay.executeQuery();

            if (rsPay.next()) {
                return new String[]{
                        rsPay.getString("id_pembayaran"),
                        rsPay.getString("tanggal_bayar"),
                        rsPay.getString("metode_pembayaran"),
                        rsPay.getString("status_pembayaran")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  daftar barang yang dibeli untuk struk
    public List<String[]> getReceiptItems(int orderId) {
        List<String[]> items = new ArrayList<>();
        String itemsQuery = "SELECT p.nama_produk, pv.ukuran, pv.warna, pd.kuantitas, pd.harga_satuan " +
                "FROM Produk_Dibeli pd " +
                "JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                "JOIN Produk p ON pv.id_produk = p.id_produk " +
                "WHERE pd.id_pesanan = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtItems = conn.prepareStatement(itemsQuery)) {

            pstmtItems.setInt(1, orderId);
            ResultSet rsItems = pstmtItems.executeQuery();

            while (rsItems.next()) {
                items.add(new String[]{
                        rsItems.getString("nama_produk"),
                        rsItems.getString("ukuran"),
                        rsItems.getString("warna"),
                        String.valueOf(rsItems.getInt("kuantitas")),
                        String.valueOf(rsItems.getDouble("harga_satuan"))
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }


    public boolean deletePendingOrder(int orderId) {
        String selectItems = "SELECT id_varian, kuantitas FROM Produk_Dibeli WHERE id_pesanan = ?";
        String updateStock = "UPDATE Produk_Varian SET stok = stok + ? WHERE id_varian = ?";

        String deleteDelivery = "DELETE FROM Pengiriman WHERE id_pesanan = ?";
        String deletePayment = "DELETE FROM Pembayaran WHERE id_pesanan = ?";
        String deleteDetails = "DELETE FROM Produk_Dibeli WHERE id_pesanan = ?";

        String deleteOrder = "DELETE FROM Pesanan WHERE id_pesanan = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmtSelect = conn.prepareStatement(selectItems);
                     PreparedStatement pstmtStock = conn.prepareStatement(updateStock)) {

                    pstmtSelect.setInt(1, orderId);
                    ResultSet rs = pstmtSelect.executeQuery();

                    while (rs.next()) {
                        pstmtStock.setInt(1, rs.getInt("kuantitas"));
                        pstmtStock.setInt(2, rs.getInt("id_varian"));
                        pstmtStock.addBatch();
                    }
                    pstmtStock.executeBatch();
                }

                try (PreparedStatement pstmtDel = conn.prepareStatement(deleteDelivery)) {
                    pstmtDel.setInt(1, orderId);
                    pstmtDel.executeUpdate();
                }

                try (PreparedStatement pstmtPay = conn.prepareStatement(deletePayment)) {
                    pstmtPay.setInt(1, orderId);
                    pstmtPay.executeUpdate();
                }

                try (PreparedStatement pstmtDetails = conn.prepareStatement(deleteDetails)) {
                    pstmtDetails.setInt(1, orderId);
                    pstmtDetails.executeUpdate();
                }

                try (PreparedStatement pstmtOrder = conn.prepareStatement(deleteOrder)) {
                    pstmtOrder.setInt(1, orderId);
                    pstmtOrder.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception ex) {
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