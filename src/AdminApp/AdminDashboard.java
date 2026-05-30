package AdminApp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminDashboard extends JFrame {
    private javax.swing.table.DefaultTableModel topProductsModel;
    private javax.swing.table.DefaultTableModel pairedItemsModel;
    private javax.swing.table.DefaultTableModel brandModel;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private JPanel ordersContainerPanel;

    private JPanel productsContainerPanel;
    private JComboBox<Category> adminCategoryDropdown;

    private final String dbURL = "jdbc:sqlserver://localhost:1433;databaseName=NIG_Clothing;encrypt=true;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "password";

    public AdminDashboard() {
        setTitle("NIG Clothing - Admin Control Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Add Navigation Bar
        add(createAdminNavBar(), BorderLayout.NORTH);

        // Add Pages to CardLayout
        mainContentPanel.add(createOrdersPage(), "ORDERS");
        // We will build these other tabs later!
        mainContentPanel.add(createProductsPage(), "PRODUCTS");
        mainContentPanel.add(createAnalyticsPage(), "ANALYTICS");

        add(mainContentPanel, BorderLayout.CENTER);

        // Show Orders by default and load the data
        cardLayout.show(mainContentPanel, "ORDERS");
        refreshOrdersData();
    }

    private JPanel createAdminNavBar() {
        String[] tabs = {"Manage Orders", "Manage Products", "Analytics"};
        String[] cardNames = {"ORDERS", "PRODUCTS", "ANALYTICS"};

        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(20, 20, 20)); // Even darker theme for Admin
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Admin Logo Area
        JLabel logoLabel = new JLabel("  NIG Clothing | ADMIN");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logoLabel.setForeground(new Color(220, 50, 50)); // Red text for admin warning
        navBar.add(logoLabel, BorderLayout.WEST);

        // Tabs
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);


        for (int i = 0; i < tabs.length; i++) {
            JButton tabButton = new JButton(tabs[i]);
            tabButton.setFont(new Font("Arial", Font.BOLD, 14));
            tabButton.setForeground(Color.WHITE);
            tabButton.setBackground(new Color(60, 60, 60));
            tabButton.setFocusPainted(false);

            final String targetCard = cardNames[i];
            tabButton.addActionListener(e -> {
                if (targetCard.equals("ORDERS")) {
                    refreshOrdersData();
                }
                cardLayout.show(mainContentPanel, targetCard);
            });
            buttonPanel.add(tabButton);
        }

        // Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            new AdminApp().setVisible(true);
            this.dispose();
        });
        buttonPanel.add(logoutBtn);

        navBar.add(buttonPanel, BorderLayout.EAST);
        return navBar;
    }

    private JPanel createPlaceholderPage(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 36));
        panel.add(label);
        return panel;
    }

    // ==========================================
    // THE ORDERS PAGE
    // ==========================================
    private JPanel createOrdersPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(245, 245, 245));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Customer Orders");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(title);
        page.add(header, BorderLayout.NORTH);

        ordersContainerPanel = new JPanel();
        ordersContainerPanel.setLayout(new BoxLayout(ordersContainerPanel, BoxLayout.Y_AXIS));
        ordersContainerPanel.setBackground(new Color(245, 245, 245));
        ordersContainerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(ordersContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        page.add(scrollPane, BorderLayout.CENTER);

        return page;
    }

    private void refreshOrdersData() {
        ordersContainerPanel.removeAll();

        // Join Pesanan with Pelanggan to get the actual name of the buyer!
        String query = "SELECT p.id_pesanan, c.nama_pelanggan, p.status_pesanan, p.tanggal_pesanan " +
                "FROM Pesanan p " +
                "JOIN Pelanggan c ON p.id_pelanggan = c.id_pelanggan " +
                "ORDER BY p.tanggal_pesanan DESC";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id_pesanan");
                String customerName = rs.getString("nama_pelanggan");
                String status = rs.getString("status_pesanan");
                String date = rs.getString("tanggal_pesanan");

                ordersContainerPanel.add(createAdminOrderCard(id, customerName, status, date));
                ordersContainerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            ordersContainerPanel.revalidate();
            ordersContainerPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createAdminOrderCard(int orderId, String customerName, String status, String date) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(1000, 100));

        // Left: Order Info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        JLabel idLabel = new JLabel("Order #" + orderId + " - " + customerName);
        idLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Color code the status!
        JLabel statusLabel = new JLabel("Status: " + status + "  |  Date: " + (date != null ? date.substring(0, 10) : "N/A"));
        if (status.equals("Pending")) statusLabel.setForeground(Color.RED);
        else if (status.equals("Selesai")) statusLabel.setForeground(new Color(0, 150, 0));
        else statusLabel.setForeground(Color.BLUE);

        infoPanel.add(idLabel);
        infoPanel.add(statusLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        // Right: The Two Management Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewItemsBtn = new JButton("View Items");
        viewItemsBtn.setBackground(new Color(100, 100, 100));
        viewItemsBtn.setForeground(Color.WHITE);
        viewItemsBtn.setFocusPainted(false);
        viewItemsBtn.addActionListener(e -> showOrderItemsPopUp(orderId));

        JButton manageBtn = new JButton("Manage Order");
        manageBtn.setBackground(new Color(50, 150, 250));
        manageBtn.setForeground(Color.WHITE);
        manageBtn.setFocusPainted(false);
        manageBtn.addActionListener(e -> showManageOrderPopUp(orderId, status));

        btnPanel.add(viewItemsBtn);
        btnPanel.add(manageBtn);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    // ==========================================
    // POP-UP 1: VIEW PURCHASED ITEMS
    // ==========================================
    private void showOrderItemsPopUp(int orderId) {
        JDialog dialog = new JDialog(this, "Items for Order #" + orderId, true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        itemsArea.setMargin(new Insets(10, 10, 10, 10));

        // Use the exact same JOIN from your customer receipt!
        String query = "SELECT p.nama_produk, pv.ukuran, pv.warna, pd.kuantitas " +
                "FROM Produk_Dibeli pd " +
                "JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                "JOIN Produk p ON pv.id_produk = p.id_produk " +
                "WHERE pd.id_pesanan = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder sb = new StringBuilder("Items Purchased:\n------------------------\n");
            while (rs.next()) {
                sb.append("- ").append(rs.getInt("kuantitas")).append("x ");
                sb.append(rs.getString("nama_produk")).append("\n  (");
                sb.append(rs.getString("ukuran")).append(" | ");
                sb.append(rs.getString("warna")).append(")\n\n");
            }
            itemsArea.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            itemsArea.setText("Error loading items.");
        }

        dialog.add(new JScrollPane(itemsArea));
        dialog.setVisible(true);
    }

    // ==========================================
    // POP-UP 2: MANAGE ORDER & DELIVERY
    // ==========================================
    private void showManageOrderPopUp(int orderId, String currentStatus) {
        JDialog dialog = new JDialog(this, "Manage Order #" + orderId, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Status Dropdown
        gbc.gridy = 0; dialog.add(new JLabel("Update Order Status:"), gbc);
        gbc.gridy = 1;
        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Pending", "Diproses", "Dibatalkan", "Selesai"});
        statusDropdown.setSelectedItem(currentStatus);
        dialog.add(statusDropdown, gbc);

        // 2. Delivery Tracking Fields
        gbc.gridy = 2; dialog.add(new JLabel("Tracking Number (Resi):"), gbc);
        gbc.gridy = 3;
        JTextField resiField = new JTextField();
        dialog.add(resiField, gbc);

        gbc.gridy = 4; dialog.add(new JLabel("Delivery Fee (Biaya):"), gbc);
        gbc.gridy = 5;
        JTextField feeField = new JTextField("0"); // Defaults to 0 as you requested
        dialog.add(feeField, gbc);

        // 3. Save Button
        gbc.gridy = 6;
        JButton saveBtn = new JButton("Save Updates");
        saveBtn.setBackground(new Color(50, 200, 100));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> {
            String newStatus = (String) statusDropdown.getSelectedItem();
            String resi = resiField.getText();
            double fee = 0;
            try { fee = Double.parseDouble(feeField.getText()); } catch (Exception ex) {}

            // SPEC 4: Using a CallableStatement to trigger our custom Stored Procedure
            String callProcedure = "{call sp_ProcessDelivery(?, ?, ?, ?)}";

            try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
                 CallableStatement cstmt = conn.prepareCall(callProcedure)) {

                // Pass the inputs directly into the SQL Stored Procedure
                cstmt.setInt(1, orderId);
                cstmt.setString(2, newStatus);
                cstmt.setString(3, resi);
                cstmt.setDouble(4, fee);

                // Execute the procedure
                cstmt.execute();

                JOptionPane.showMessageDialog(dialog, "Delivery Status Updated Successfully!");
                dialog.dispose();
                refreshOrdersData(); // Instantly re-draws the list with the new colors!

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error updating delivery.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    // ==========================================
    // THE PRODUCTS PAGE
    // ==========================================
    private JPanel createProductsPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(245, 245, 245));

        // Top Header & Category Filter
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Product Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        JButton addProductBtn = new JButton("+ Add New Product");
        addProductBtn.setBackground(new Color(50, 200, 100));
        addProductBtn.setForeground(Color.WHITE);
        addProductBtn.setFocusPainted(false);
        addProductBtn.addActionListener(e -> showAddProductPopUp());
        filterPanel.add(addProductBtn);
        // Add some spacing
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(new JLabel("Filter by Category: "));

        adminCategoryDropdown = new JComboBox<>();
        adminCategoryDropdown.addItem(new Category(0, "All Categories"));

        // Load categories from DB
        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_kategori, nama_kategori FROM Kategori")) {
            while (rs.next()) {
                adminCategoryDropdown.addItem(new Category(rs.getInt("id_kategori"), rs.getString("nama_kategori")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add action listener to refresh products when a category is picked
        adminCategoryDropdown.addActionListener(e -> {
            Category selected = (Category) adminCategoryDropdown.getSelectedItem();
            if (selected != null) {
                loadAdminProducts(selected.getId());
            }
        });

        filterPanel.add(adminCategoryDropdown);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        page.add(headerPanel, BorderLayout.NORTH);

        // Products Container
        productsContainerPanel = new JPanel();
        productsContainerPanel.setLayout(new BoxLayout(productsContainerPanel, BoxLayout.Y_AXIS));
        productsContainerPanel.setBackground(new Color(245, 245, 245));
        productsContainerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(productsContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        page.add(scrollPane, BorderLayout.CENTER);

        // Initial Load (Category 0 = All)
        loadAdminProducts(0);

        return page;
    }

    // ==========================================
    // LOAD PRODUCTS FROM DB
    // ==========================================
    private void loadAdminProducts(int categoryId) {
        productsContainerPanel.removeAll();

        String query = (categoryId == 0)
                ? "SELECT id_produk, nama_produk FROM Produk"
                : "SELECT id_produk, nama_produk FROM Produk WHERE id_kategori = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (categoryId != 0) pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_produk");
                String name = rs.getString("nama_produk");

                // Create a simple card for each product
                JPanel card = new JPanel(new BorderLayout());
                card.setBackground(Color.WHITE);
                card.setMaximumSize(new Dimension(800, 60));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));

                JLabel nameLabel = new JLabel(name);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                card.add(nameLabel, BorderLayout.WEST);

                JButton manageBtn = new JButton("Manage Stock");
                manageBtn.setBackground(new Color(50, 150, 250));
                manageBtn.setForeground(Color.WHITE);
                manageBtn.setFocusPainted(false);
                manageBtn.addActionListener(e -> showProductStockPopUp(id, name));

                JButton changeBrandBtn = new JButton("Change Brand");
                changeBrandBtn.setBackground(new Color(150, 50, 250));
                changeBrandBtn.setForeground(Color.WHITE);
                changeBrandBtn.setFocusPainted(false);
                changeBrandBtn.addActionListener(e -> showChangeBrandPopUp(id, name));

                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(220, 50, 50));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFocusPainted(false);
                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteProductFromDB(id);
                    }
                });

                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                actionPanel.setOpaque(false);
                actionPanel.add(manageBtn);
                actionPanel.add(changeBrandBtn);
                actionPanel.add(deleteBtn);
                card.add(actionPanel, BorderLayout.EAST);

                productsContainerPanel.add(card);
                productsContainerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            productsContainerPanel.revalidate();
            productsContainerPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // VARIANT STOCK MANAGEMENT POP-UP
    // ==========================================
    private void showProductStockPopUp(int productId, String productName) {
        JDialog dialog = new JDialog(this, "Manage Stock - " + productName, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Variant Dropdown
        gbc.gridy = 0; dialog.add(new JLabel("Select Variant (Size | Color):"), gbc);
        gbc.gridy = 1;
        JComboBox<AdminVariant> variantDropdown = new JComboBox<>();
        boolean hasVariants = false;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_varian, ukuran, warna, stok FROM Produk_Varian WHERE id_produk = ?")) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hasVariants = true;
                variantDropdown.addItem(new AdminVariant(rs.getInt("id_varian"), rs.getString("ukuran"), rs.getString("warna"), rs.getInt("stok")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hasVariants) {
            JOptionPane.showMessageDialog(this, "This product has no variants in the database yet!");
            return; // Abort pop-up if empty
        }
        dialog.add(variantDropdown, gbc);

        // 2. New Stock Input
        gbc.gridy = 2; dialog.add(new JLabel("Set New Stock Amount:"), gbc);
        gbc.gridy = 3;

        // Use a JSpinner to prevent them from typing letters!
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        dialog.add(stockSpinner, gbc);

        // Smart UI: When they change the dropdown, update the spinner to match that variant's current stock!
        variantDropdown.addActionListener(e -> {
            AdminVariant selected = (AdminVariant) variantDropdown.getSelectedItem();
            if (selected != null) {
                stockSpinner.setValue(selected.getStock());
            }
        });

        // Trigger it once manually to set the initial value
        stockSpinner.setValue(((AdminVariant) variantDropdown.getSelectedItem()).getStock());

        // 3. Save Button
        gbc.gridy = 4;
        JButton saveBtn = new JButton("Update Stock");
        saveBtn.setBackground(new Color(50, 200, 100));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> {
            AdminVariant selected = (AdminVariant) variantDropdown.getSelectedItem();
            int newStock = (Integer) stockSpinner.getValue();

            if (selected != null) {
                try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE Produk_Varian SET stok = ? WHERE id_varian = ?")) {

                    pstmt.setInt(1, newStock);
                    pstmt.setInt(2, selected.getId());
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(dialog, "Stock updated successfully!");
                    dialog.dispose(); // Close pop-up

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    // ==========================================
    // THE ANALYTICS PAGE
    // ==========================================
    private JPanel createAnalyticsPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(new Color(245, 245, 245));
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Store Analytics & Insights");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(title);
        page.add(headerPanel, BorderLayout.NORTH);

        // Grid to hold the two tables side-by-side
        JPanel tableContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        tableContainer.setOpaque(false);

        // --- Table 1: Top Selling Products (NOW WITH TIME FILTER!) ---
        JPanel topProductsPanel = new JPanel(new BorderLayout());
        topProductsPanel.setBackground(Color.WHITE);
        topProductsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Highest Selling Products"));

        // NEW: The Dropdown UI
        JPanel topProductsHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topProductsHeader.setOpaque(false);
        topProductsHeader.add(new JLabel("Filter Time: "));

        String[] timeOptions = {"All Time", "Last 24 Hours", "Last 7 Days", "Last 30 Days"};
        JComboBox<String> timeFilterDropdown = new JComboBox<>(timeOptions);
        topProductsHeader.add(timeFilterDropdown);

        // This line is what actually glues the dropdown to the screen!
        topProductsPanel.add(topProductsHeader, BorderLayout.NORTH);

        String[] col1 = {"Product Name", "Total Units Sold"};
        topProductsModel = new javax.swing.table.DefaultTableModel(col1, 0);
        JTable topProductsTable = new JTable(topProductsModel);
        topProductsTable.setRowHeight(25);
        topProductsPanel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);

        // Action Listener to refresh ONLY this table when the dropdown changes
        timeFilterDropdown.addActionListener(e -> {
            refreshTopProductsData((String) timeFilterDropdown.getSelectedItem());
        });

        // --- Table 2: Frequently Bought Together ---
        JPanel pairedPanel = new JPanel(new BorderLayout());
        pairedPanel.setBackground(Color.WHITE);
        pairedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Frequently Bought Together"));

        String[] col2 = {"Item A", "Item B", "Times Paired"};
        pairedItemsModel = new javax.swing.table.DefaultTableModel(col2, 0);
        JTable pairedTable = new JTable(pairedItemsModel);
        pairedTable.setRowHeight(25);
        pairedPanel.add(new JScrollPane(pairedTable), BorderLayout.CENTER);

        tableContainer.add(topProductsPanel);
        tableContainer.add(pairedPanel);
        page.add(tableContainer, BorderLayout.CENTER);

        // Note: Acknowledging the "Brand" analytics feature!
        // We have left space at the bottom to add the Brand filters once you alter your database.
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(Color.WHITE);
        brandPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Top Product by Brand"));

        String[] col3 = {"Brand", "Top Product", "Sold"};
        brandModel = new javax.swing.table.DefaultTableModel(col3, 0);
        JTable brandTable = new JTable(brandModel);
        brandTable.setRowHeight(25);
        brandPanel.add(new JScrollPane(brandTable), BorderLayout.CENTER);

        // Make sure all THREE are added to the container!
        tableContainer.add(topProductsPanel);
        tableContainer.add(pairedPanel);
        tableContainer.add(brandPanel);

        return page;
    }

    // ==========================================
    // THE ANALYTICS SQL LOGIC
    // ==========================================
    private void refreshAnalyticsData() {
        pairedItemsModel.setRowCount(0);
        brandModel.setRowCount(0);

        refreshTopProductsData("All Time");

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {

            // 1. QUERY: Top Selling Products (Overall)
            // --- Table 1: Top Selling Products (NOW WITH TIME FILTER!) ---
            JPanel topProductsPanel = new JPanel(new BorderLayout());
            topProductsPanel.setBackground(Color.WHITE);
            topProductsPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.GRAY), "Highest Selling Products"));

            // NEW: The Dropdown UI
            JPanel topProductsHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            topProductsHeader.setOpaque(false);
            topProductsHeader.add(new JLabel("Filter Time: "));

            String[] timeOptions = {"All Time", "Last 24 Hours", "Last 7 Days", "Last 30 Days"};
            JComboBox<String> timeFilterDropdown = new JComboBox<>(timeOptions);
            topProductsHeader.add(timeFilterDropdown);
            topProductsPanel.add(topProductsHeader, BorderLayout.NORTH); // Put it at the top!

            String[] col1 = {"Product Name", "Total Units Sold"};
            topProductsModel = new javax.swing.table.DefaultTableModel(col1, 0);
            JTable topProductsTable = new JTable(topProductsModel);
            topProductsTable.setRowHeight(25);
            topProductsPanel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);

            // NEW: Action Listener to refresh ONLY this table when the dropdown changes
            timeFilterDropdown.addActionListener(e -> {
                refreshTopProductsData((String) timeFilterDropdown.getSelectedItem());
            });

            // 2. QUERY: Frequently Bought Together (Self-Join Magic)
            // We join the table to itself using the Order ID, ensuring Item A's ID < Item B's ID
            // so we don't get duplicates like (Shirt, Hat) and (Hat, Shirt).
            String pairQuery =
                    "SELECT TOP 10 p1.nama_produk AS item1, p2.nama_produk AS item2, COUNT(*) AS times_paired " +
                            "FROM Produk_Dibeli pd1 " +
                            "JOIN Produk_Varian pv1 ON pd1.id_varian = pv1.id_varian " +
                            "JOIN Produk p1 ON pv1.id_produk = p1.id_produk " +
                            "JOIN Produk_Dibeli pd2 ON pd1.id_pesanan = pd2.id_pesanan AND pv1.id_produk < pv2.id_produk " +
                            "JOIN Produk_Varian pv2 ON pd2.id_varian = pv2.id_varian " +
                            "JOIN Produk p2 ON pv2.id_produk = p2.id_produk " +
                            "GROUP BY p1.nama_produk, p2.nama_produk " +
                            "ORDER BY times_paired DESC";

            try (Statement stmt2 = conn.createStatement();
                 ResultSet rs2 = stmt2.executeQuery(pairQuery)) {
                while (rs2.next()) {
                    pairedItemsModel.addRow(new Object[]{
                            rs2.getString("item1"),
                            rs2.getString("item2"),
                            rs2.getInt("times_paired") + " orders"
                    });
                }
                // 3. QUERY: Highest Selling Product Per Brand
                // We use a CTE (WITH clause) to rank products within their brand by sales
                String brandQuery =
                        "WITH BrandSales AS (" +
                                "    SELECT b.nama_brand, p.nama_produk, SUM(pd.kuantitas) as total_sold, " +
                                "           ROW_NUMBER() OVER(PARTITION BY b.id_brand ORDER BY SUM(pd.kuantitas) DESC) as rank " +
                                "    FROM Produk_Dibeli pd " +
                                "    JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                                "    JOIN Produk p ON pv.id_produk = p.id_produk " +
                                "    JOIN Brand b ON p.id_brand = b.id_brand " +
                                "    GROUP BY b.id_brand, b.nama_brand, p.id_produk, p.nama_produk" +
                                ") " +
                                "SELECT nama_brand, nama_produk, total_sold " +
                                "FROM BrandSales " +
                                "WHERE rank = 1";

                try (Statement stmt3 = conn.createStatement();
                     ResultSet rs3 = stmt3.executeQuery(brandQuery)) {
                    while (rs3.next()) {
                        brandModel.addRow(new Object[]{
                                rs3.getString("nama_brand"),
                                rs3.getString("nama_produk"),
                                rs3.getInt("total_sold") + " units"
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Analytics Error: Make sure your database has enough sample orders!");
        }
    }

    private void refreshTopProductsData(String timeFilter) {
        topProductsModel.setRowCount(0); // Clear the table instantly
        String dateCondition = ""; // Default to "All Time" (No WHERE clause)

        // Inject the SQL Server time filters based on the dropdown choice
        if (timeFilter.equals("Last 24 Hours")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -1, GETDATE()) ";
        } else if (timeFilter.equals("Last 7 Days")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -7, GETDATE()) ";
        } else if (timeFilter.equals("Last 30 Days")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -30, GETDATE()) ";
        }

        // We added a JOIN to the Pesanan table so we can check the 'tanggal_pesanan'
        String query =
                "SELECT TOP 10 p.nama_produk, SUM(pd.kuantitas) AS total_sold " +
                        "FROM Produk_Dibeli pd " +
                        "JOIN Pesanan pes ON pd.id_pesanan = pes.id_pesanan " +
                        "JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                        "JOIN Produk p ON pv.id_produk = p.id_produk " +
                        dateCondition + // This dynamically inserts the WHERE clause if needed!
                        "GROUP BY p.nama_produk " +
                        "ORDER BY total_sold DESC";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                topProductsModel.addRow(new Object[]{
                        rs.getString("nama_produk"),
                        rs.getInt("total_sold") + " units"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to fetch Brands from the database dynamically
    private JComboBox<model.Brand> createBrandDropdown() {
        JComboBox<model.Brand> dropdown = new JComboBox<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_brand, nama_brand FROM Brand")) {
            while (rs.next()) {
                dropdown.addItem(new model.Brand(rs.getInt("id_brand"), rs.getString("nama_brand")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dropdown;
    }

    // UPDATED: Create Product (Now includes Brand!)
    private void showAddProductPopUp() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        JComboBox<Category> catDropdown = new JComboBox<>();
        for (int i = 1; i < adminCategoryDropdown.getItemCount(); i++) {
            catDropdown.addItem(adminCategoryDropdown.getItemAt(i));
        }

        // NEW: Brand Dropdown!
        JComboBox<model.Brand> brandDropdown = createBrandDropdown();

        JTextField sizeField = new JTextField("L");
        JTextField colorField = new JTextField("Black");
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 9999, 1));

        Object[] message = {
                "--- PRODUCT INFO ---", "",
                "Product Name:", nameField,
                "Price (Rp):", priceField,
                "Category:", catDropdown,
                "Brand:", brandDropdown, // Added to the UI!
                "", "",
                "--- FIRST VARIANT INFO ---", "",
                "Size (e.g., S, M, L, XL):", sizeField,
                "Color:", colorField,
                "Initial Stock:", stockSpinner
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            // NOTICE: id_brand is now a ? instead of a hardcoded 1
            String insertProduct = "INSERT INTO Produk (nama_produk, harga, id_kategori, id_brand) VALUES (?, ?, ?, ?)";
            String insertVariant = "INSERT INTO Produk_Varian (id_produk, ukuran, warna, stok) VALUES (?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmtProd = conn.prepareStatement(insertProduct, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtProd.setString(1, nameField.getText());
                    pstmtProd.setDouble(2, Double.parseDouble(priceField.getText()));
                    pstmtProd.setInt(3, ((Category) catDropdown.getSelectedItem()).getId());
                    // Set the brand ID based on what the Admin selected!
                    pstmtProd.setInt(4, ((model.Brand) brandDropdown.getSelectedItem()).getId());
                    pstmtProd.executeUpdate();

                    ResultSet rs = pstmtProd.getGeneratedKeys();
                    if (rs.next()) {
                        int newProductId = rs.getInt(1);
                        try (PreparedStatement pstmtVar = conn.prepareStatement(insertVariant)) {
                            pstmtVar.setInt(1, newProductId);
                            pstmtVar.setString(2, sizeField.getText());
                            pstmtVar.setString(3, colorField.getText());
                            pstmtVar.setInt(4, (Integer) stockSpinner.getValue());
                            pstmtVar.executeUpdate();
                        }
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Product successfully created!");
                    loadAdminProducts(0);
                } catch (Exception ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error creating product.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // NEW METHOD: Update an existing product's brand!
    private void showChangeBrandPopUp(int productId, String productName) {
        JComboBox<model.Brand> brandDropdown = createBrandDropdown();

        Object[] message = {
                "Select a new Brand for:",
                "<html><b>" + productName + "</b></html>",
                "",
                brandDropdown
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Change Brand", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            model.Brand selectedBrand = (model.Brand) brandDropdown.getSelectedItem();

            if (selectedBrand != null) {
                String query = "UPDATE Produk SET id_brand = ? WHERE id_produk = ?";
                try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setInt(1, selectedBrand.getId());
                    pstmt.setInt(2, productId);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Brand successfully updated to " + selectedBrand.getName() + "!");
                    // Refresh the store so the customer sees the change instantly
                    loadAdminProducts(0);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error updating brand.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteProductFromDB(int productId) {
        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Produk WHERE id_produk = ?")) {

            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            loadAdminProducts(0); // Refresh the list

        } catch (SQLException e) {
            // THIS CATCHES THE TRIGGER WE MADE EARLIER!
            JOptionPane.showMessageDialog(this, "Cannot delete product! It has already been purchased by customers.", "Delete Blocked", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==========================================
    // HELPER CLASSES
    // ==========================================
    class Category {
        private int id;
        private String name;
        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        @Override
        public String toString() { return name; }
    }

    class AdminVariant {
        private int id;
        private String size;
        private String color;
        private int stock;

        public AdminVariant(int id, String size, String color, int stock) {
            this.id = id;
            this.size = size;
            this.color = color;
            this.stock = stock;
        }
        public int getId() { return id; }
        public int getStock() { return stock; }
        @Override
        public String toString() {
            return size + " | " + color + " (Current Stock: " + stock + ")";
        }
    }
}