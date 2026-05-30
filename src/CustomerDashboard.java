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

    private JPanel cartItemsContainer;
    private JTextArea billTextArea;
    private JLabel cartTotalLabel;

    private java.util.List<CartItem> floatingCart = new java.util.ArrayList<>();

    private JPanel purchasedContainerPanel;

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
        mainContentPanel.add(createCartPage(), "CART");
        mainContentPanel.add(createPurchasedPage(), "PURCHASED");
        mainContentPanel.add(createPlaceholderPage("Account Settings"), "ACCOUNT");

        add(mainContentPanel, BorderLayout.CENTER);
        cardLayout.show(mainContentPanel, "STORE");
    }

    private JPanel createNavBar() {
        // 1. Change the main container to BorderLayout
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(30, 30, 30));
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Adds nice padding

        // ==========================================
        // 2. THE LOGO (Far Left)
        // ==========================================
        ImageIcon logoIcon = scaleImage("pictures/Logo.png", 50, true);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setText("  NIG Clothing");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE); // Keeps text visible on dark background

        navBar.add(logoLabel, BorderLayout.WEST);

        // ==========================================
        // 3. THE BUTTONS (Far Right)
        // ==========================================
        // Create a transparent sub-panel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        String[] tabs = {"Store", "Cart", "Purchased", "Account"};
        String[] cardNames = {"STORE", "CART", "PURCHASED", "ACCOUNT"};

        // Your existing loop logic stays exactly the same!
        for (int i = 0; i < tabs.length; i++) {
            JButton tabButton = new JButton(tabs[i]);
            tabButton.setFont(new Font("Arial", Font.BOLD, 16));
            tabButton.setForeground(Color.WHITE);
            tabButton.setBackground(new Color(50, 50, 50));
            tabButton.setFocusPainted(false);

            final String targetCard = cardNames[i];
            tabButton.addActionListener(e -> {
                if (targetCard.equals("CART")) {
                    refreshCartData();
                } else if (targetCard.equals("PURCHASED")) {
                    refreshPurchasedData();
                }
                cardLayout.show(mainContentPanel, targetCard);
            });

            buttonPanel.add(tabButton);
        }

        // Add the grouped buttons to the right side
        navBar.add(buttonPanel, BorderLayout.EAST);

        return navBar;
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

        // 1. UPDATED SQL QUERIES: Added 'gambar_produk' to both queries
        String query = (categoryId == 0)
                ? "SELECT id_produk, nama_produk, harga, gambar_produk FROM Produk"
                : "SELECT id_produk, nama_produk, harga, gambar_produk FROM Produk WHERE id_kategori = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (categoryId != 0) pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            while (rs.next()) {
                // 2. UPDATED METHOD CALL: Pass the new image string as the 4th parameter
                productGridPanel.add(createProductCard(
                        rs.getInt("id_produk"),
                        rs.getString("nama_produk"),
                        rpFormat.format(rs.getDouble("harga")),
                        rs.getString("gambar_produk") // <-- This is the new piece!
                ));
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
// FIX 1: Added 'String imageName' to the parameters here!
    private JPanel createProductCard(int id, String name, String formattedPrice, String imageName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // ==========================================
        // SMART IMAGE LOADER
        // ==========================================
        String finalImagePath;

        if (imageName != null && !imageName.trim().isEmpty()) {
            finalImagePath = "pictures/products/" + imageName;

            java.io.File imgFile = new java.io.File(finalImagePath);
            if (!imgFile.exists()) {
                finalImagePath = "pictures/placeholder.png";
            }
        } else {
            finalImagePath = "pictures/placeholder.png";
        }

        // FIX 2: Fixed the variable names to 'imgLabel' consistently
        JLabel imgLabel = new JLabel(scaleImage(finalImagePath, 150, true));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // FIX 3: Put the image at the top (NORTH) so it doesn't fight with the text
        card.add(imgLabel, BorderLayout.NORTH);

        // --- Info Panel ---
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel priceLabel = new JLabel(formattedPrice, SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(0, 150, 0));
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        // Put the text in the middle (CENTER)
        card.add(infoPanel, BorderLayout.CENTER);

        // --- Button ---
        JButton buyButton = new JButton("Select Options");
        buyButton.setBackground(new Color(50, 150, 250));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);

        buyButton.addActionListener(e -> {
            double rawPrice = Double.parseDouble(formattedPrice.replaceAll("[^\\d.]", ""));
            showVariantPopUp(id, name, rawPrice);
        });

        // Put the button at the bottom (SOUTH)
        card.add(buyButton, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // UPDATED: THE VARIANT POP-UP DIALOG
    // ==========================================
    private void showVariantPopUp(int productId, String productName, double price) {
        JDialog dialog = new JDialog(this, "Select Options - " + productName, true);
        dialog.setSize(400, 300); // Made it slightly taller to fit the new input
        dialog.setLocationRelativeTo(this);
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
        String query = "SELECT id_varian, ukuran, warna, stok FROM Produk_Varian WHERE id_produk = ? AND stok > 0";
        boolean hasVariants = false;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hasVariants = true;
                variantDropdown.addItem(new Variant(
                        rs.getInt("id_varian"), rs.getString("ukuran"), rs.getString("warna"), rs.getInt("stok")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!hasVariants) {
            JOptionPane.showMessageDialog(this, "Sorry, this product is currently out of stock!");
            return;
        }
        mainPanel.add(variantDropdown, gbc);

        // ==========================================
        // NEW: QUANTITY SPINNER
        // ==========================================
        gbc.gridy = 2;
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(new JLabel("Quantity:  "));

        // Default 1, Min 1, Max 99 (we will cap it dynamically), Step 1
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtyPanel.add(quantitySpinner);
        mainPanel.add(qtyPanel, gbc);

        // 3. Add to Cart Button
        gbc.gridy = 3;
        JButton confirmButton = new JButton("Confirm Add to Cart");
        confirmButton.setBackground(new Color(50, 200, 100));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));

        confirmButton.addActionListener(e -> {
            Variant selectedVariant = (Variant) variantDropdown.getSelectedItem();
            int selectedQty = (Integer) quantitySpinner.getValue(); // Read the spinner!

            if (selectedVariant != null) {
                // Ensure they don't try to buy more than is in stock
                if (selectedQty > selectedVariant.stock) {
                    JOptionPane.showMessageDialog(dialog, "Only " + selectedVariant.stock + " items left in stock!", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean alreadyInCart = false;
                for (CartItem item : floatingCart) {
                    if (item.getVariantId() == selectedVariant.getId()) {
                        item.setQuantity(item.getQuantity() + selectedQty); // Add the specific amount
                        alreadyInCart = true;
                        break;
                    }
                }

                if (!alreadyInCart) {
                    String info = selectedVariant.getSize() + " | " + selectedVariant.getColor();
                    floatingCart.add(new CartItem(selectedVariant.getId(), productName, info, price, selectedQty));
                }

                JOptionPane.showMessageDialog(dialog, "Added " + selectedQty + "x " + productName + " to cart!");
                dialog.dispose();
            }
        });

        mainPanel.add(confirmButton, gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ==========================================
    // THE CART PAGE
    // ==========================================
    private JPanel createCartPage() {
        JPanel cartPage = new JPanel(new BorderLayout(20, 0)); // 20px gap between left and right
        cartPage.setBackground(new Color(245, 245, 245));
        cartPage.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ------------------------------------------
        // LEFT PANEL: Cart Items
        // ------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        // Header (Title & Remove All Button)
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);
        JLabel cartTitle = new JLabel("Shopping Cart");
        cartTitle.setFont(new Font("Arial", Font.BOLD, 28));

        JButton removeAllBtn = new JButton("Remove All");
        removeAllBtn.setBackground(new Color(220, 50, 50));
        removeAllBtn.setForeground(Color.WHITE);
        removeAllBtn.setFocusPainted(false);
        removeAllBtn.addActionListener(e -> {
            clearEntireCart(); // We will build this helper method below!
        });

        leftHeader.add(cartTitle, BorderLayout.WEST);
        leftHeader.add(removeAllBtn, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Container for the individual items
        cartItemsContainer = new JPanel();
        cartItemsContainer.setLayout(new BoxLayout(cartItemsContainer, BoxLayout.Y_AXIS));
        cartItemsContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(cartItemsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        cartPage.add(leftPanel, BorderLayout.CENTER);

        // ------------------------------------------
        // RIGHT PANEL: The Bill / Order Summary
        // ------------------------------------------
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setPreferredSize(new Dimension(350, 0)); // Fixed width for the bill
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 22));
        rightPanel.add(summaryTitle, BorderLayout.NORTH);

        // The Text-based Bill
        billTextArea = new JTextArea();
        billTextArea.setEditable(false);
        billTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        billTextArea.setBorder(null);
        rightPanel.add(new JScrollPane(billTextArea), BorderLayout.CENTER);

        // Checkout Section (Bottom)
        JPanel checkoutPanel = new JPanel(new BorderLayout(0, 15));
        checkoutPanel.setOpaque(false);

        cartTotalLabel = new JLabel("Total: Rp 0", SwingConstants.RIGHT);
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        checkoutPanel.add(cartTotalLabel, BorderLayout.NORTH);

        JButton checkoutBtn = new JButton("Continue to Purchase");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutBtn.setBackground(new Color(50, 200, 100));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setPreferredSize(new Dimension(0, 50)); // Tall button
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.addActionListener(e -> {
            showCheckoutPopUp();
        });
        checkoutPanel.add(checkoutBtn, BorderLayout.SOUTH);

        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);
        cartPage.add(rightPanel, BorderLayout.EAST);

        return cartPage;
    }

    private void refreshCartData() {
        cartItemsContainer.removeAll();
        StringBuilder billText = new StringBuilder();
        double grandTotal = 0.0;
        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Loop through the floating Java list
        for (int i = 0; i < floatingCart.size(); i++) {
            CartItem item = floatingCart.get(i);
            double subtotal = item.getPrice() * item.getQuantity();
            grandTotal += subtotal;

            // Pass 'i' (the list index) instead of a database ID
            cartItemsContainer.add(createCartItemCard(i, item.getProductName(), item.getVariantInfo(), item.getPrice(), item.getQuantity()));

            billText.append(item.getProductName()).append(" (").append(item.getVariantInfo()).append(")\n");
            billText.append(item.getQuantity()).append(" x ").append(rpFormat.format(item.getPrice())).append("\n");
            billText.append("Subtotal: ").append(rpFormat.format(subtotal)).append("\n\n");
        }

        billTextArea.setText(billText.toString());
        cartTotalLabel.setText("Total: " + rpFormat.format(grandTotal));

        cartItemsContainer.revalidate();
        cartItemsContainer.repaint();
    }

    // ==========================================
    // UPDATED: UI FOR A SINGLE CART ITEM ROW
    // ==========================================
    private JPanel createCartItemCard(int listIndex, String name, String variantInfo, double price, int qty) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 0, 10);

        // 1. Image
        gbc.gridx = 0; gbc.weightx = 0;
        JLabel imgLabel = new JLabel(scaleImage("pictures/placeholder.png", 80, true));
        card.add(imgLabel, gbc);

        // 2. Details
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1));
        detailsPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel variantLabel = new JLabel(variantInfo);
        variantLabel.setForeground(Color.GRAY);
        JLabel priceLabel = new JLabel(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(price));
        priceLabel.setForeground(new Color(0, 150, 0));
        detailsPanel.add(nameLabel);
        detailsPanel.add(variantLabel);
        detailsPanel.add(priceLabel);
        card.add(detailsPanel, gbc);

        // ==========================================
        // NEW: THE PLUS / MINUS QUANTITY CONTROLS
        // ==========================================
        gbc.gridx = 2; gbc.weightx = 0;
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        qtyPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setMargin(new Insets(2, 6, 2, 6)); // Make it a small square
        minusBtn.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel qtyLabel = new JLabel(String.valueOf(qty));
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton plusBtn = new JButton("+");
        plusBtn.setMargin(new Insets(2, 6, 2, 6));
        plusBtn.setFont(new Font("Arial", Font.BOLD, 14));

        // Logic to decrease quantity
        minusBtn.addActionListener(e -> {
            if (qty > 1) { // Prevents going below 1 (they must use 'Remove' to delete)
                floatingCart.get(listIndex).setQuantity(qty - 1);
                refreshCartData(); // Redraw instantly!
            }
        });

        // Logic to increase quantity
        plusBtn.addActionListener(e -> {
            floatingCart.get(listIndex).setQuantity(qty + 1);
            refreshCartData(); // Redraw instantly!
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);
        card.add(qtyPanel, gbc);

        // 4. Remove Button
        gbc.gridx = 3;
        JButton removeBtn = new JButton("Remove");
        removeBtn.setBackground(new Color(220, 50, 50));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.addActionListener(e -> {
            floatingCart.remove(listIndex);
            refreshCartData();
        });
        card.add(removeBtn, gbc);

        return card;
    }

    private void clearEntireCart() {
        floatingCart.clear(); // Wipes the memory list clean instantly!
        refreshCartData();
    }

    // ==========================================
    // THE PURCHASED PAGE
    // ==========================================
    private JPanel createPurchasedPage() {
        JPanel purchasedPage = new JPanel(new BorderLayout());
        purchasedPage.setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Your Order History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        purchasedPage.add(headerPanel, BorderLayout.NORTH);

        // Container for the order cards
        purchasedContainerPanel = new JPanel();
        purchasedContainerPanel.setLayout(new BoxLayout(purchasedContainerPanel, BoxLayout.Y_AXIS));
        purchasedContainerPanel.setBackground(new Color(245, 245, 245));
        purchasedContainerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(purchasedContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        purchasedPage.add(scrollPane, BorderLayout.CENTER);

        return purchasedPage;
    }

    // ==========================================
    // FETCH ORDER HISTORY LOGIC
    // ==========================================
    private void refreshPurchasedData() {
        purchasedContainerPanel.removeAll();

        // We join Pesanan and Pelanggan to only get orders for the logged-in user!
        String query = "SELECT p.id_pesanan, p.tanggal_pesanan, p.status_pesanan, p.total_harga " +
                "FROM Pesanan p " +
                "JOIN Pelanggan cust ON p.id_pelanggan = cust.id_pelanggan " +
                "WHERE cust.Username = ? " +
                "ORDER BY p.tanggal_pesanan DESC"; // Newest orders at the top

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInUser);
            ResultSet rs = pstmt.executeQuery();

            NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            boolean hasOrders = false;

            while (rs.next()) {
                hasOrders = true;
                int idPesanan = rs.getInt("id_pesanan");
                String date = rs.getString("tanggal_pesanan");
                String status = rs.getString("status_pesanan");
                double total = rs.getDouble("total_harga");

                purchasedContainerPanel.add(createOrderCard(idPesanan, date, status, rpFormat.format(total)));
                purchasedContainerPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Gap between cards
            }

            if (!hasOrders) {
                JLabel emptyLabel = new JLabel("You haven't placed any orders yet.");
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                purchasedContainerPanel.add(emptyLabel);
            }

            purchasedContainerPanel.revalidate();
            purchasedContainerPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // UPDATED: INDIVIDUAL ORDER CARD UI
    // ==========================================
    private JPanel createOrderCard(int idPesanan, String date, String status, String formattedTotal) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(800, 130)); // Slightly taller to fit two buttons

        // Left Side: Order Info
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        infoPanel.setOpaque(false);

        JLabel dateLabel = new JLabel("Order Date: " + (date != null ? date.substring(0, 10) : "N/A"));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setForeground(Color.DARK_GRAY);

        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel priceLabel = new JLabel("Total: " + formattedTotal);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(0, 150, 0));

        infoPanel.add(dateLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(priceLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        // ==========================================
        // NEW: Right Side (Two Buttons Stacked)
        // ==========================================
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 rows, 1 column, 10px vertical gap
        btnPanel.setOpaque(false);

        JButton trackBtn = new JButton("Check Delivery");
        trackBtn.setBackground(new Color(50, 150, 250));
        trackBtn.setForeground(Color.WHITE);
        trackBtn.setFocusPainted(false);
        trackBtn.setFont(new Font("Arial", Font.BOLD, 12));
        trackBtn.addActionListener(e -> showDeliveryPopUp(idPesanan));

        JButton billBtn = new JButton("View Bill Details");
        billBtn.setBackground(new Color(100, 100, 100)); // Dark grey button for contrast
        billBtn.setForeground(Color.WHITE);
        billBtn.setFocusPainted(false);
        billBtn.setFont(new Font("Arial", Font.BOLD, 12));
        billBtn.addActionListener(e -> showPaymentPopUp(idPesanan)); // Calls our new method!

        btnPanel.add(trackBtn);
        btnPanel.add(billBtn);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    // ==========================================
    // DELIVERY TRACKING POP-UP
    // ==========================================
    private void showDeliveryPopUp(int idPesanan) {
        // Join Pengiriman and Pengirim to get all courier details at once
        String query = "SELECT p.tanggal_kirim, p.nomor_resi, p.status_pengiriman, p.biaya_pengiriman, " +
                "e.nama_ekspedisi, e.nomor_telepon_pengirim " +
                "FROM Pengiriman p " +
                "JOIN Pengirim e ON p.id_pengirim = e.id_pengirim " +
                "WHERE p.id_pesanan = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idPesanan);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // We found shipping data!
                JDialog dialog = new JDialog(this, "Delivery Details (Order #" + idPesanan + ")", true);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                String sentDate = rs.getString("tanggal_kirim");
                String trackingNo = rs.getString("nomor_resi");
                String status = rs.getString("status_pengiriman");
                double fee = rs.getDouble("biaya_pengiriman"); // This will read 0.0 if you default it
                String courierName = rs.getString("nama_ekspedisi");
                String contactStr = rs.getString("nomor_telepon_pengirim");

                NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                panel.add(new JLabel("<html><b>Courier:</b> " + courierName + "</html>"));
                panel.add(new JLabel("<html><b>Contact:</b> " + (contactStr != null ? contactStr : "N/A") + "</html>"));
                panel.add(new JLabel("<html><b>Date Sent:</b> " + (sentDate != null ? sentDate.substring(0, 10) : "Pending") + "</html>"));
                panel.add(new JLabel("<html><b>Tracking Number:</b> " + (trackingNo != null ? trackingNo : "Awaiting Info") + "</html>"));
                panel.add(new JLabel("<html><b>Status:</b> " + (status != null ? status : "N/A") + "</html>"));
                panel.add(new JLabel("<html><b>Delivery Fee:</b> " + rpFormat.format(fee) + "</html>"));

                dialog.add(panel);
                dialog.setVisible(true);

            } else {
                // Order exists, but Admin hasn't generated a Pengiriman row yet
                JOptionPane.showMessageDialog(this,
                        "Your order is currently being processed by our team.\nDelivery details will be available once the package is handed to the courier.",
                        "Tracking Unavailable",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load delivery data.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // UPDATED: PAYMENT BILL POP-UP (WITH ITEMS)
    // ==========================================
    private void showPaymentPopUp(int idPesanan) {
        JDialog dialog = new JDialog(this, "Order Receipt (#" + idPesanan + ")", true);
        dialog.setSize(400, 550); // Made it taller to fit the item list!
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // We use a JTextArea with a Monospaced font so it looks like a real printed receipt
        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptArea.setMargin(new Insets(15, 15, 15, 15));

        StringBuilder receiptText = new StringBuilder();
        receiptText.append("========================================\n");
        receiptText.append("          NIG CLOTHING RECEIPT          \n");
        receiptText.append("========================================\n\n");

        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        double grandTotal = 0;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {

            // 1. Get Payment Info (Header of the receipt)
            String payQuery = "SELECT id_pembayaran, tanggal_bayar, metode_pembayaran, status_pembayaran FROM Pembayaran WHERE id_pesanan = ?";
            try (PreparedStatement pstmtPay = conn.prepareStatement(payQuery)) {
                pstmtPay.setInt(1, idPesanan);
                ResultSet rsPay = pstmtPay.executeQuery();
                if (rsPay.next()) {
                    String payDate = rsPay.getString("tanggal_bayar");
                    String displayDate = (payDate != null && payDate.length() >= 19) ? payDate.substring(0, 19) : payDate;

                    receiptText.append("Receipt No : #").append(rsPay.getString("id_pembayaran")).append("\n");
                    receiptText.append("Date       : ").append(displayDate).append("\n");
                    receiptText.append("Method     : ").append(rsPay.getString("metode_pembayaran")).append("\n");
                    receiptText.append("Status     : ").append(rsPay.getString("status_pembayaran")).append("\n\n");
                }
            }

            receiptText.append("----------------------------------------\n");
            receiptText.append("PURCHASED ITEMS:\n");
            receiptText.append("----------------------------------------\n");

            // 2. Get Items Info (Body of the receipt)
            // We use JOINs here to translate the IDs back into readable names and colors!
            String itemsQuery = "SELECT p.nama_produk, pv.ukuran, pv.warna, pd.kuantitas, pd.harga_satuan " +
                    "FROM Produk_Dibeli pd " +
                    "JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                    "JOIN Produk p ON pv.id_produk = p.id_produk " +
                    "WHERE pd.id_pesanan = ?";

            try (PreparedStatement pstmtItems = conn.prepareStatement(itemsQuery)) {
                pstmtItems.setInt(1, idPesanan);
                ResultSet rsItems = pstmtItems.executeQuery();

                while (rsItems.next()) {
                    String name = rsItems.getString("nama_produk");
                    String variant = rsItems.getString("ukuran") + " | " + rsItems.getString("warna");
                    int qty = rsItems.getInt("kuantitas");
                    double price = rsItems.getDouble("harga_satuan");
                    double subtotal = qty * price;

                    grandTotal += subtotal;

                    receiptText.append(name).append(" (").append(variant).append(")\n");
                    receiptText.append(qty).append(" x ").append(rpFormat.format(price)).append("\n");
                    receiptText.append("Subtotal: ").append(rpFormat.format(subtotal)).append("\n\n");
                }
            }

            receiptText.append("----------------------------------------\n");
            receiptText.append("GRAND TOTAL: ").append(rpFormat.format(grandTotal)).append("\n");
            receiptText.append("========================================\n");
            receiptText.append("        Thank you for shopping!         \n");

        } catch (Exception e) {
            e.printStackTrace();
            receiptText.append("\n[Error loading receipt details from database]");
        }

        receiptArea.setText(receiptText.toString());

        // Add the receipt to the center (with scrolling in case they bought a lot of stuff)
        dialog.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        // Add a simple close button at the bottom
        JButton closeBtn = new JButton("Close Receipt");
        closeBtn.setBackground(new Color(220, 50, 50));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

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

    // ==========================================
    // THE CHECKOUT POP-UP
    // ==========================================
    private void showCheckoutPopUp() {
        if (floatingCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Checkout Confirmation", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Order Summary Text Area
        gbc.gridy = 0;
        JLabel summaryLabel = new JLabel("Review Your Items:");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(summaryLabel, gbc);

        gbc.gridy = 1;
        JTextArea summaryArea = new JTextArea(8, 30);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        double grandTotal = 0;

        for (CartItem item : floatingCart) {
            summaryArea.append("- " + item.getQuantity() + "x " + item.getProductName() + "\n");
            grandTotal += (item.getPrice() * item.getQuantity());
        }

        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        summaryArea.append("\nGrand Total: " + rpFormat.format(grandTotal));
        mainPanel.add(new JScrollPane(summaryArea), gbc);

        // 2. Courier Dropdown
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Select Courier:"), gbc);

        gbc.gridy = 3;
        JComboBox<Courier> courierDropdown = new JComboBox<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_pengirim, nama_ekspedisi FROM Pengirim")) {
            while (rs.next()) {
                courierDropdown.addItem(new Courier(rs.getInt("id_pengirim"), rs.getString("nama_ekspedisi")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainPanel.add(courierDropdown, gbc);

        // 3. Payment Method Dropdown
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Select Payment Method:"), gbc);

        gbc.gridy = 5;
        JComboBox<String> paymentDropdown = new JComboBox<>(new String[]{"Cash On Delivery", "Online Payment"});
        mainPanel.add(paymentDropdown, gbc);

        // 4. Confirm Button
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 20, 20, 20);
        JButton confirmOrderBtn = new JButton("Confirm Order");
        confirmOrderBtn.setBackground(new Color(50, 200, 100));
        confirmOrderBtn.setForeground(Color.WHITE);
        confirmOrderBtn.setFont(new Font("Arial", Font.BOLD, 16));

        final double finalTotal = grandTotal; // Needed for the lambda expression

        confirmOrderBtn.addActionListener(e -> {
            Courier selectedCourier = (Courier) courierDropdown.getSelectedItem();
            String selectedPayment = (String) paymentDropdown.getSelectedItem();

            if (selectedCourier != null) {
                // Execute the database transaction
                boolean success = processCheckoutTransaction(finalTotal, selectedCourier, selectedPayment);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Purchase Successful! Thank you for shopping.");
                    dialog.dispose();
                    clearEntireCart(); // Empty the floating cart
                    cardLayout.show(mainContentPanel, "STORE"); // Send user back to store
                } else {
                    JOptionPane.showMessageDialog(dialog, "Transaction failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainPanel.add(confirmOrderBtn, gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ==========================================
    // THE SQL TRANSACTION (WITH STOCK REDUCTION)
    // ==========================================
    private boolean processCheckoutTransaction(double totalHarga, Courier courier, String paymentMethod) {
        int customerId = -1;
        String getCustomerSql = "SELECT id_pelanggan FROM Pelanggan WHERE Username = ?";
        String insertPesananSql = "INSERT INTO Pesanan (id_pelanggan, tanggal_pesanan, status_pesanan, total_harga) VALUES (?, GETDATE(), ?, ?)";
        String insertDetailSql = "INSERT INTO Produk_Dibeli (id_pesanan, id_varian, kuantitas, harga_satuan) VALUES (?, ?, ?, ?)";
        String insertPembayaranSql = "INSERT INTO Pembayaran (id_pesanan, tanggal_bayar, metode_pembayaran, jumlah_bayar, status_pembayaran) VALUES (?, GETDATE(), ?, ?, ?)";

        // ==========================================
        // NEW: SQL to reduce the stock
        // ==========================================
        String updateStockSql = "UPDATE Produk_Varian SET stok = stok - ? WHERE id_varian = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            // Get the Customer ID
            try (PreparedStatement pstmtCustomer = conn.prepareStatement(getCustomerSql)) {
                pstmtCustomer.setString(1, loggedInUser);
                ResultSet rsCustomer = pstmtCustomer.executeQuery();
                if (rsCustomer.next()) {
                    customerId = rsCustomer.getInt("id_pelanggan");
                } else {
                    System.out.println("Customer not found!");
                    return false;
                }
            }

            // --- START TRANSACTION ---
            conn.setAutoCommit(false);

            try {
                int newPesananId = -1;

                // A. Create the Order Header
                try (PreparedStatement pstmtPesanan = conn.prepareStatement(insertPesananSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtPesanan.setInt(1, customerId);
                    pstmtPesanan.setString(2, "Pending (via " + courier.getName() + ")");
                    pstmtPesanan.setDouble(3, totalHarga);
                    pstmtPesanan.executeUpdate();

                    ResultSet generatedKeys = pstmtPesanan.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        newPesananId = generatedKeys.getInt(1);
                    }
                }

                // B & D. Create Order Details AND Reduce Stock simultaneously
                try (PreparedStatement pstmtDetail = conn.prepareStatement(insertDetailSql);
                     PreparedStatement pstmtStock = conn.prepareStatement(updateStockSql)) {

                    for (CartItem item : floatingCart) {
                        // Queue up the receipt details
                        pstmtDetail.setInt(1, newPesananId);
                        pstmtDetail.setInt(2, item.getVariantId());
                        pstmtDetail.setInt(3, item.getQuantity());
                        pstmtDetail.setDouble(4, item.getPrice());
                        pstmtDetail.addBatch();

                        // Queue up the stock reduction
                        pstmtStock.setInt(1, item.getQuantity()); // The amount to subtract
                        pstmtStock.setInt(2, item.getVariantId()); // The exact size/color ID
                        pstmtStock.addBatch();
                    }

                    // Execute both batches of queries at the exact same time
                    pstmtDetail.executeBatch();
                    pstmtStock.executeBatch();
                }

                // C. Create the Payment Record
                try (PreparedStatement pstmtPembayaran = conn.prepareStatement(insertPembayaranSql)) {
                    pstmtPembayaran.setInt(1, newPesananId);
                    pstmtPembayaran.setString(2, paymentMethod);
                    pstmtPembayaran.setDouble(3, totalHarga);
                    pstmtPembayaran.setString(4, "Berhasil");
                    pstmtPembayaran.executeUpdate();
                }

                // --- END TRANSACTION ---
                conn.commit(); // Save everything!
                return true;

            } catch (SQLException ex) {
                conn.rollback(); // Cancel everything if even one step fails!
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

    // ==========================================
    // NEW: FLOATING CART ITEM OBJECT
    // ==========================================
    class CartItem {
        private int variantId;
        private String productName;
        private String variantInfo;
        private double price;
        private int quantity;

        public CartItem(int variantId, String productName, String variantInfo, double price, int quantity) {
            this.variantId = variantId;
            this.productName = productName;
            this.variantInfo = variantInfo;
            this.price = price;
            this.quantity = quantity;
        }

        public int getVariantId() { return variantId; }
        public String getProductName() { return productName; }
        public String getVariantInfo() { return variantInfo; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    // ==========================================
    // COURIER DATA OBJECT
    // ==========================================
    class Courier {
        private int id;
        private String name;

        public Courier(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override
        public String toString() { return name; } // Displays in the dropdown
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