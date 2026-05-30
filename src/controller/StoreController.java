package controller;

import model.Category;
import model.Product;
import model.Variant;
import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StoreController {

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(0, "All Categories", "Showing all available products in the store."));
        String query = "SELECT id_kategori, nama_kategori, deskripsi FROM Kategori";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id_kategori"), rs.getString("nama_kategori"), rs.getString("deskripsi")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Product> getProducts(int categoryId) {
        List<Product> products = new ArrayList<>();
        // WE ADDED A JOIN TO THE BRAND TABLE!
        String query = (categoryId == 0)
                ? "SELECT p.id_produk, p.nama_produk, p.harga, p.gambar_produk, b.nama_brand " +
                "FROM Produk p LEFT JOIN Brand b ON p.id_brand = b.id_brand"
                : "SELECT p.id_produk, p.nama_produk, p.harga, p.gambar_produk, b.nama_brand " +
                "FROM Produk p LEFT JOIN Brand b ON p.id_brand = b.id_brand WHERE p.id_kategori = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (categoryId != 0) pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga"),
                        rs.getString("gambar_produk"),
                        rs.getString("nama_brand") // Grab the brand!
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Variant> getProductVariants(int productId) {
        List<Variant> variants = new ArrayList<>();
        String query = "SELECT id_varian, ukuran, warna, stok FROM Produk_Varian WHERE id_produk = ? AND stok > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                variants.add(new Variant(
                        rs.getInt("id_varian"), rs.getString("ukuran"), rs.getString("warna"), rs.getInt("stok")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return variants;
    }

    // SPEC 3: TEXT-BASED SEARCH QUERY (Now with Brand!)
    public List<Product> searchProductsByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.id_produk, p.nama_produk, p.harga, p.gambar_produk, b.nama_brand " +
                "FROM Produk p LEFT JOIN Brand b ON p.id_brand = b.id_brand " +
                "WHERE p.nama_produk LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga"),
                        rs.getString("gambar_produk"),
                        rs.getString("nama_brand")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }
}