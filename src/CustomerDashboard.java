import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class CustomerDashboard extends JFrame {

    private String loggedInUser;
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);

    private JPanel productGridPanel;
    private JLabel categoryNameLabel;
    private JLabel categoryDescLabel;
    private JComboBox<Category> categoryDropdown;

    private final String dbURL = "jdbc:sqlserver://localhost:1433;databaseName=NIG_Clothing;encrypt=true;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "password";

    public CustomerDashboard(String username) {
        this.loggedInUser = username;

        setTitle("NIG Clothing - Dashboard (" + username + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createNavBar(), BorderLayout.NORTH);

        mainContentPanel.add(createStorePage(), "STORE");
        mainContentPanel.add(createPlaceholderPage("Cart Page"), "CART");
        mainContentPanel.add(createPlaceholderPage("Purchased Items"), "PURCHASED");
        mainContentPanel.add(createPlaceholderPage("Account Settings"), "ACCOUNT");

        add(mainContentPanel, BorderLayout.CENTER);
        cardLayout.show(mainContentPanel, "STORE");
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        navPanel.setBackground(new Color(30, 30, 30));

        String[] tabs = {"Store", "Cart", "Purchased", "Account"};
        String[] cardNames = {"STORE", "CART", "PURCHASED", "ACCOUNT"};

        for (int i = 0; i < tabs.length; i++) {
            JButton tabButton = new JButton(tabs[i]);
            tabButton.setFont(new Font("Arial", Font.BOLD, 16));
            tabButton.setForeground(Color.WHITE);
            tabButton.setBackground(new Color(50, 50, 50));
            tabButton.setFocusPainted(false);

            final String targetCard = cardNames[i];
            tabButton.addActionListener(e -> cardLayout.show(mainContentPanel, targetCard));

            navPanel.add(tabButton);
        }
        return navPanel;
    }

    private JPanel createStorePage() {
        JPanel storePage = new JPanel(new BorderLayout());
        storePage.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        categoryNameLabel = new JLabel("All Categories");
        categoryNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        categoryDescLabel = new JLabel("Showing all available products in the store.");
        categoryDescLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryDescLabel.setForeground(Color.DARK_GRAY);
        infoPanel.add(categoryNameLabel);
        infoPanel.add(categoryDescLabel);
        headerPanel.add(infoPanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Sort by Category: "));

        categoryDropdown = new JComboBox<>();
        loadCategoriesFromDB();

        categoryDropdown.addActionListener(e -> {
            Category selectedCategory = (Category) categoryDropdown.getSelectedItem();
            if (selectedCategory != null) {
                categoryNameLabel.setText(selectedCategory.getName());
                categoryDescLabel.setText(selectedCategory.getDescription());
                loadProductsFromDB(selectedCategory.getId());
            }
        });

        filterPanel.add(categoryDropdown);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        storePage.add(headerPanel, BorderLayout.NORTH);

        productGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        productGridPanel.setBackground(Color.WHITE);
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        storePage.add(scrollPane, BorderLayout.CENTER);

        loadProductsFromDB(0);

        return storePage;
    }

    private void loadCategoriesFromDB() {
        categoryDropdown.addItem(new Category(0, "All Categories", "Showing all available products in the store."));
        String query = "SELECT id_kategori, nama_kategori, deskripsi FROM Kategori";
        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categoryDropdown.addItem(new Category(rs.getInt("id_kategori"), rs.getString("nama_kategori"), rs.getString("deskripsi")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromDB(int categoryId) {
        productGridPanel.removeAll();
        String query = (categoryId == 0)
                ? "SELECT id_produk, nama_produk, harga FROM Produk"
                : "SELECT id_produk, nama_produk, harga FROM Produk WHERE id_kategori = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (categoryId != 0) pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            while (rs.next()) {
                // Pass the ID to the card builder so the button knows which product to look up!
                productGridPanel.add(createProductCard(rs.getInt("id_produk"), rs.getString("nama_produk"), rpFormat.format(rs.getDouble("harga"))));
            }
            productGridPanel.revalidate();
            productGridPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // UPDATED: PRODUCT CARD BUILDER
    // ==========================================
    private JPanel createProductCard(int id, String name, String formattedPrice) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel imageLabel = new JLabel(scaleImage("pictures/placeholder.png", 200, true));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imageLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel priceLabel = new JLabel(formattedPrice, SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(0, 150, 0));
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        JButton buyButton = new JButton("Select Options");
        buyButton.setBackground(new Color(50, 150, 250));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);

        // NEW: Instead of just adding to cart, it opens the variant pop-up!
        buyButton.addActionListener(e -> showVariantPopUp(id, name));

        card.add(buyButton, BorderLayout.SOUTH);
        return card;
    }

    // ==========================================
    // NEW: THE VARIANT POP-UP DIALOG
    // ==========================================
    private void showVariantPopUp(int productId, String productName) {
        // Create a custom pop-up dialog (true means it blocks the main window until closed)
        JDialog dialog = new JDialog(this, "Select Options - " + productName, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this); // Centers it on screen
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Title Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Choose Size and Color:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);

        // 2. The Dropdown
        gbc.gridy = 1;
        JComboBox<Variant> variantDropdown = new JComboBox<>();

        // --- DATABASE: Load Variants ---
        // UPDATE "Varian" below if your actual table name is slightly different (like VarianProduk)
        String query = "SELECT id_varian, ukuran, warna, stok FROM Produk_Varian WHERE id_produk = ? AND stok > 0";
        boolean hasVariants = false;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                hasVariants = true;
                variantDropdown.addItem(new Variant(
                        rs.getInt("id_varian"),
                        rs.getString("ukuran"),
                        rs.getString("warna"),
                        rs.getInt("stok")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hasVariants) {
            JOptionPane.showMessageDialog(this, "Sorry, this product is currently out of stock!");
            return; // Don't show the pop up if nothing is in stock
        }

        mainPanel.add(variantDropdown, gbc);

        // 3. Add to Cart Button
        gbc.gridy = 2;
        JButton confirmButton = new JButton("Confirm Add to Cart");
        confirmButton.setBackground(new Color(50, 200, 100)); // Nice Green
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));

        confirmButton.addActionListener(e -> {
            Variant selectedVariant = (Variant) variantDropdown.getSelectedItem();
            if (selectedVariant != null) {
                // TODO: Here is where you will do the SQL INSERT into your Shopping Cart table!
                // You have access to: loggedInUser, productId, and selectedVariant.getId()

                JOptionPane.showMessageDialog(dialog, "Added " + productName + " (" + selectedVariant.getSize() + " - " + selectedVariant.getColor() + ") to cart!");
                dialog.dispose(); // Close the pop-up
            }
        });

        mainPanel.add(confirmButton, gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ==========================================
    // HELPER CLASSES & METHODS
    // ==========================================

    // NEW: Variant Data Object
    class Variant {
        private int id;
        private String size;
        private String color;
        private int stock;

        public Variant(int id, String size, String color, int stock) {
            this.id = id;
            this.size = size;
            this.color = color;
            this.stock = stock;
        }

        public int getId() { return id; }
        public String getSize() { return size; }
        public String getColor() { return color; }

        @Override
        public String toString() {
            // This dictates exactly how the text looks inside the dropdown!
            return size + " | " + color + " (Stock: " + stock + ")";
        }
    }

    // Category Data Object
    class Category {
        private int id;
        private String name;
        private String description;

        public Category(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        @Override
        public String toString() { return name; }
    }

    private JPanel createPlaceholderPage(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 36));
        panel.add(label);
        return panel;
    }

    private ImageIcon scaleImage(String imagePath, int targetSize, boolean lockWidth) {
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image originalImage = originalIcon.getImage();
        if (originalImage == null || originalIcon.getIconWidth() == -1) return originalIcon;
        Image scaledImage;
        if (lockWidth) scaledImage = originalImage.getScaledInstance(targetSize, -1, Image.SCALE_SMOOTH);
        else scaledImage = originalImage.getScaledInstance(-1, targetSize, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}