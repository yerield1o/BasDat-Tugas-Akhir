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

        add(createAdminNavBar(), BorderLayout.NORTH);

        mainContentPanel.add(createOrdersPage(), "ORDERS");
        mainContentPanel.add(createProductsPage(), "PRODUCTS");
        mainContentPanel.add(createAnalyticsPage(), "ANALYTICS");

        add(mainContentPanel, BorderLayout.CENTER);

        cardLayout.show(mainContentPanel, "ORDERS");
        refreshOrdersData();
        refreshAnalyticsData("All Time");
    }

    private JPanel createAdminNavBar() {
        String[] tabs = {"Manage Orders", "Manage Products", "Analytics"};
        String[] cardNames = {"ORDERS", "PRODUCTS", "ANALYTICS"};

        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(20, 20, 20));
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel("  NIG Clothing | ADMIN");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logoLabel.setForeground(new Color(220, 50, 50));
        navBar.add(logoLabel, BorderLayout.WEST);

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

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        JLabel idLabel = new JLabel("Order #" + orderId + " - " + customerName);
        idLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel statusLabel = new JLabel("Status: " + status + "  |  Date: " + (date != null ? date.substring(0, 10) : "N/A"));
        if (status.equals("Pending")) statusLabel.setForeground(Color.RED);
        else if (status.equals("Selesai")) statusLabel.setForeground(new Color(0, 150, 0));
        else statusLabel.setForeground(Color.BLUE);

        infoPanel.add(idLabel);
        infoPanel.add(statusLabel);
        card.add(infoPanel, BorderLayout.CENTER);

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

    private void showOrderItemsPopUp(int orderId) {
        JDialog dialog = new JDialog(this, "Items for Order #" + orderId, true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        itemsArea.setMargin(new Insets(10, 10, 10, 10));

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

    private void showManageOrderPopUp(int orderId, String currentStatus) {
        JDialog dialog = new JDialog(this, "Manage Order #" + orderId, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0; dialog.add(new JLabel("Update Order Status:"), gbc);
        gbc.gridy = 1;
        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Pending", "Diproses", "Dibatalkan", "Selesai"});
        statusDropdown.setSelectedItem(currentStatus);
        dialog.add(statusDropdown, gbc);

        gbc.gridy = 2; dialog.add(new JLabel("Tracking Number (Resi):"), gbc);
        gbc.gridy = 3;
        JTextField resiField = new JTextField();
        dialog.add(resiField, gbc);

        gbc.gridy = 4; dialog.add(new JLabel("Delivery Fee (Biaya):"), gbc);
        gbc.gridy = 5;
        JTextField feeField = new JTextField("0");
        dialog.add(feeField, gbc);

        gbc.gridy = 6;
        JButton saveBtn = new JButton("Save Updates");
        saveBtn.setBackground(new Color(50, 200, 100));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> {
            String newStatus = (String) statusDropdown.getSelectedItem();
            String resi = resiField.getText();
            double fee = 0;
            try { fee = Double.parseDouble(feeField.getText()); } catch (Exception ex) {}

            String callProcedure = "{call sp_ProcessDelivery(?, ?, ?, ?)}";

            try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
                 CallableStatement cstmt = conn.prepareCall(callProcedure)) {

                cstmt.setInt(1, orderId);
                cstmt.setString(2, newStatus);
                cstmt.setString(3, resi);
                cstmt.setDouble(4, fee);

                cstmt.execute();

                JOptionPane.showMessageDialog(dialog, "Delivery Status Updated Successfully!");
                dialog.dispose();
                refreshOrdersData();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error updating delivery.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private JPanel createProductsPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(245, 245, 245));

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
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(new JLabel("Filter by Category: "));

        adminCategoryDropdown = new JComboBox<>();
        adminCategoryDropdown.addItem(new Category(0, "All Categories"));

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_kategori, nama_kategori FROM Kategori")) {
            while (rs.next()) {
                adminCategoryDropdown.addItem(new Category(rs.getInt("id_kategori"), rs.getString("nama_kategori")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adminCategoryDropdown.addActionListener(e -> {
            Category selected = (Category) adminCategoryDropdown.getSelectedItem();
            if (selected != null) {
                loadAdminProducts(selected.getId());
            }
        });

        filterPanel.add(adminCategoryDropdown);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        page.add(headerPanel, BorderLayout.NORTH);

        productsContainerPanel = new JPanel();
        productsContainerPanel.setLayout(new BoxLayout(productsContainerPanel, BoxLayout.Y_AXIS));
        productsContainerPanel.setBackground(new Color(245, 245, 245));
        productsContainerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(productsContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        page.add(scrollPane, BorderLayout.CENTER);

        loadAdminProducts(0);

        return page;
    }

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

    private void showProductStockPopUp(int productId, String productName) {
        JDialog dialog = new JDialog(this, "Manage Stock - " + productName, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
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
            return;
        }
        dialog.add(variantDropdown, gbc);

        gbc.gridy = 2; dialog.add(new JLabel("Set New Stock Amount:"), gbc);
        gbc.gridy = 3;

        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        dialog.add(stockSpinner, gbc);

        variantDropdown.addActionListener(e -> {
            AdminVariant selected = (AdminVariant) variantDropdown.getSelectedItem();
            if (selected != null) {
                stockSpinner.setValue(selected.getStock());
            }
        });

        stockSpinner.setValue(((AdminVariant) variantDropdown.getSelectedItem()).getStock());

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
                    dialog.dispose();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private JPanel createAnalyticsPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(new Color(245, 245, 245));
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Store Analytics & Insights");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(title);
        page.add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        tableContainer.setOpaque(false);

        JPanel topProductsPanel = new JPanel(new BorderLayout());
        topProductsPanel.setBackground(Color.WHITE);
        topProductsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Highest Selling Products"));

        JPanel topProductsHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topProductsHeader.setOpaque(false);
        topProductsHeader.add(new JLabel("Filter Time: "));

        String[] timeOptions = {"All Time", "Last 24 Hours", "Last 7 Days", "Last 30 Days"};
        JComboBox<String> timeFilterDropdown = new JComboBox<>(timeOptions);
        topProductsHeader.add(timeFilterDropdown);

        topProductsPanel.add(topProductsHeader, BorderLayout.NORTH);

        String[] col1 = {"Product Name", "Total Units Sold"};
        topProductsModel = new javax.swing.table.DefaultTableModel(col1, 0);
        JTable topProductsTable = new JTable(topProductsModel);
        topProductsTable.setRowHeight(25);
        topProductsPanel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);

        timeFilterDropdown.addActionListener(e -> {
            String selectedTime = (String) timeFilterDropdown.getSelectedItem();
            refreshAnalyticsData(selectedTime);
        });

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

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(Color.WHITE);
        brandPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Top Product by Brand"));

        String[] col3 = {"Brand", "Top Product", "Sold"};
        brandModel = new javax.swing.table.DefaultTableModel(col3, 0);
        JTable brandTable = new JTable(brandModel);
        brandTable.setRowHeight(25);
        brandPanel.add(new JScrollPane(brandTable), BorderLayout.CENTER);

        tableContainer.add(topProductsPanel);
        tableContainer.add(pairedPanel);
        tableContainer.add(brandPanel);

        return page;
    }

    private void refreshAnalyticsData(String timeFilter) {
        pairedItemsModel.setRowCount(0);
        brandModel.setRowCount(0);

        refreshTopProductsData(timeFilter);

        int filterDays = 36500;
        if (timeFilter.equals("Last 24 Hours")) filterDays = 1;
        else if (timeFilter.equals("Last 7 Days")) filterDays = 7;
        else if (timeFilter.equals("Last 30 Days")) filterDays = 30;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            String pairQuery =
                    "SELECT TOP 10 p1.nama_produk AS item1, p2.nama_produk AS item2, COUNT(*) AS times_paired " +
                            "FROM Produk_Dibeli pd1 " +
                            "JOIN Produk_Varian pv1 ON pd1.id_varian = pv1.id_varian " +
                            "JOIN Produk p1 ON pv1.id_produk = p1.id_produk " +
                            "JOIN Produk_Dibeli pd2 ON pd1.id_pesanan = pd2.id_pesanan AND pd1.id_varian < pd2.id_varian " +
                            "JOIN Produk_Varian pv2 ON pd2.id_varian = pv2.id_varian " +
                            "JOIN Produk p2 ON pv2.id_produk = p2.id_produk " +
                            "JOIN Pesanan pes ON pd1.id_pesanan = pes.id_pesanan " +
                            "WHERE pes.tanggal_pesanan >= DATEADD(day, -" + filterDays + ", GETDATE()) " +
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
            }


            try (CallableStatement cstmt = conn.prepareCall("{call sp_Top5Brands(?)}")) {
                cstmt.setInt(1, filterDays);

                try (ResultSet rs3 = cstmt.executeQuery()) {
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
            System.out.println("Analytics Error: Failed to fetch dynamic data.");
        }
    }

    private void refreshTopProductsData(String timeFilter) {
        topProductsModel.setRowCount(0);
        String dateCondition = "";

        if (timeFilter.equals("Last 24 Hours")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -1, GETDATE()) ";
        } else if (timeFilter.equals("Last 7 Days")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -7, GETDATE()) ";
        } else if (timeFilter.equals("Last 30 Days")) {
            dateCondition = "WHERE pes.tanggal_pesanan >= DATEADD(day, -30, GETDATE()) ";
        }

        String query =
                "SELECT TOP 10 p.nama_produk, SUM(pd.kuantitas) AS total_sold " +
                        "FROM Produk_Dibeli pd " +
                        "JOIN Pesanan pes ON pd.id_pesanan = pes.id_pesanan " +
                        "JOIN Produk_Varian pv ON pd.id_varian = pv.id_varian " +
                        "JOIN Produk p ON pv.id_produk = p.id_produk " +
                        dateCondition +
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

    private void showAddProductPopUp() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        JComboBox<Category> catDropdown = new JComboBox<>();
        for (int i = 1; i < adminCategoryDropdown.getItemCount(); i++) {
            catDropdown.addItem(adminCategoryDropdown.getItemAt(i));
        }

        JComboBox<model.Brand> brandDropdown = createBrandDropdown();

        JTextField sizeField = new JTextField("L");
        JTextField colorField = new JTextField("Black");
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 9999, 1));

        Object[] message = {
                "--- PRODUCT INFO ---", "",
                "Product Name:", nameField,
                "Price (Rp):", priceField,
                "Category:", catDropdown,
                "Brand:", brandDropdown,
                "", "",
                "--- FIRST VARIANT INFO ---", "",
                "Size (e.g., S, M, L, XL):", sizeField,
                "Color:", colorField,
                "Initial Stock:", stockSpinner
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            String insertProduct = "INSERT INTO Produk (nama_produk, harga, id_kategori, id_brand, gambar_produk) VALUES (?, ?, ?, ?, 'default.png')";
            String insertVariant = "INSERT INTO Produk_Varian (id_produk, ukuran, warna, stok) VALUES (?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmtProd = conn.prepareStatement(insertProduct, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtProd.setString(1, nameField.getText());
                    pstmtProd.setDouble(2, Double.parseDouble(priceField.getText()));
                    pstmtProd.setInt(3, ((Category) catDropdown.getSelectedItem()).getId());
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
            loadAdminProducts(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot delete product! It has already been purchased by customers.", "Delete Blocked", JOptionPane.WARNING_MESSAGE);
        }
    }


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