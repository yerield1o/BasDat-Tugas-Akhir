package view.component;

import controller.StoreController;
import model.CartItem;
import model.Product;
import model.Variant;
import util.ImageLoader;
import view.dashboard.DashboardFrame;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductCard extends JPanel {
    private Product product;
    private DashboardFrame parentFrame;
    private StoreController storeController;

    public ProductCard(Product product, DashboardFrame parentFrame, StoreController storeController) {
        this.product = product;
        this.parentFrame = parentFrame;
        this.storeController = storeController;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Gambar
        String finalImagePath = (product.getImageName() != null && !product.getImageName().trim().isEmpty())
                ? "pictures/products/" + product.getImageName() : "pictures/placeholder.png";

        java.io.File imgFile = new java.io.File(finalImagePath);
        if (!imgFile.exists()) finalImagePath = "pictures/placeholder.png";

        JLabel imgLabel = new JLabel(ImageLoader.scaleImage(finalImagePath, 150, true));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(imgLabel, BorderLayout.NORTH);

        // Info Produk
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(product.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        JLabel priceLabel = new JLabel(rpFormat.format(product.getPrice()), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(0, 150, 0));

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        add(infoPanel, BorderLayout.CENTER);

        // Tombol Pilih
        JButton buyButton = new JButton("Select Options");
        buyButton.setBackground(new Color(50, 150, 250));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);
        buyButton.addActionListener(e -> showVariantPopUp());
        add(buyButton, BorderLayout.SOUTH);
    }

    private void showVariantPopUp() {
        JDialog dialog = new JDialog(parentFrame, "Select Options - " + product.getName(), true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Choose Size and Color:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        JComboBox<Variant> variantDropdown = new JComboBox<>();
        List<Variant> variants = storeController.getProductVariants(product.getId());

        if (variants.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Sorry, this product is currently out of stock!");
            return;
        }
        for (Variant v : variants) variantDropdown.addItem(v);
        mainPanel.add(variantDropdown, gbc);

        gbc.gridy = 2;
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(new JLabel("Quantity:  "));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtyPanel.add(quantitySpinner);
        mainPanel.add(qtyPanel, gbc);

        gbc.gridy = 3;
        JButton confirmButton = new JButton("Confirm Add to Cart");
        confirmButton.setBackground(new Color(50, 200, 100));
        confirmButton.setForeground(Color.WHITE);

        confirmButton.addActionListener(e -> {
            Variant selectedVariant = (Variant) variantDropdown.getSelectedItem();
            int selectedQty = (Integer) quantitySpinner.getValue();

            if (selectedVariant != null) {
                List<CartItem> cart = parentFrame.getFloatingCart();
                int amountAlreadyInCart = 0;
                CartItem existingCartItem = null;

                for (CartItem item : cart) {
                    if (item.getVariantId() == selectedVariant.getId()) {
                        amountAlreadyInCart = item.getQuantity();
                        existingCartItem = item;
                        break;
                    }
                }

                if ((selectedQty + amountAlreadyInCart) > selectedVariant.getStock()) {
                    JOptionPane.showMessageDialog(dialog, "Stock limited. Only " + selectedVariant.getStock() + " left.", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (existingCartItem != null) {
                    existingCartItem.setQuantity(existingCartItem.getQuantity() + selectedQty);
                } else {
                    String info = selectedVariant.getSize() + " | " + selectedVariant.getColor();
                    cart.add(new CartItem(selectedVariant.getId(), product.getName(), info, product.getPrice(), selectedQty, selectedVariant.getStock()));
                }

                JOptionPane.showMessageDialog(dialog, "Added to cart!");
                dialog.dispose();
            }
        });

        mainPanel.add(confirmButton, gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}