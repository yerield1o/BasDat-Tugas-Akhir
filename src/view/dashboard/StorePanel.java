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
}