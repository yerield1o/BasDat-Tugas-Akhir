package view.dashboard;

import controller.StoreController;
import model.Category;
import model.Product;
import view.component.ProductCard;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StorePanel extends JPanel {
    private DashboardFrame parentFrame;
    private StoreController storeController;
    private JPanel productGridPanel;
    private JLabel categoryNameLabel;
    private JLabel categoryDescLabel;
    private JComboBox<Category> categoryDropdown;

    public StorePanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.storeController = new StoreController();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        categoryNameLabel = new JLabel("All Categories");
        categoryNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        categoryDescLabel = new JLabel("Showing all available products in the store.");
        infoPanel.add(categoryNameLabel);
        infoPanel.add(categoryDescLabel);
        headerPanel.add(infoPanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(50, 150, 250));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                loadSearchedProducts(keyword);
            } else {
                // If they clear the search bar and hit search, reset the store
                categoryDropdown.setSelectedIndex(0);
                loadProducts(0);
            }
        });

        filterPanel.add(searchField);
        filterPanel.add(searchBtn);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        filterPanel.add(new JLabel("Sort by Category: "));

        categoryDropdown = new JComboBox<>();
        List<Category> categories = storeController.getCategories();
        for (Category c : categories) categoryDropdown.addItem(c);

        categoryDropdown.addActionListener(e -> {
            Category selectedCategory = (Category) categoryDropdown.getSelectedItem();
            if (selectedCategory != null) {
                categoryNameLabel.setText(selectedCategory.getName());
                categoryDescLabel.setText(selectedCategory.getDescription());
                loadProducts(selectedCategory.getId());
            }
        });
        filterPanel.add(categoryDropdown);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        productGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        productGridPanel.setBackground(Color.WHITE);
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Load All Products by default
        loadProducts(0);
    }

    private void loadProducts(int categoryId) {
        productGridPanel.removeAll();
        List<Product> products = storeController.getProducts(categoryId);

        for (Product p : products) {
            // Memanggil ProductCard secara utuh
            productGridPanel.add(new ProductCard(p, parentFrame, storeController));
        }

        productGridPanel.revalidate();
        productGridPanel.repaint();
    }
    public void refreshStoreData() {
        Category selectedCategory = (Category) categoryDropdown.getSelectedItem();
        int categoryId = (selectedCategory != null) ? selectedCategory.getId() : 0;
        loadProducts(categoryId);
    }
    // Helper method to load search results
    private void loadSearchedProducts(String keyword) {
        productGridPanel.removeAll();

        // Update the headers so the user knows they are searching
        categoryNameLabel.setText("Search Results");
        categoryDescLabel.setText("Showing results for: \"" + keyword + "\"");

        List<Product> products = storeController.searchProductsByName(keyword);

        if (products.isEmpty()) {
            JLabel emptyLabel = new JLabel("No products found matching that name.");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            productGridPanel.add(emptyLabel);
        } else {
            for (Product p : products) {
                productGridPanel.add(new ProductCard(p, parentFrame, storeController));
            }
        }

        productGridPanel.revalidate();
        productGridPanel.repaint();
    }
}