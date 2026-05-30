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
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(Color.WHITE);

        String brandText = (product.getBrandName() != null) ? product.getBrandName().toUpperCase() : "NO BRAND";
        JLabel brandLabel = new JLabel(brandText, SwingConstants.CENTER);
        brandLabel.setFont(new Font("Arial", Font.BOLD, 12));
        brandLabel.setForeground(Color.GRAY);

        JLabel nameLabel = new JLabel(product.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        JLabel priceLabel = new JLabel(rpFormat.format(product.getPrice()), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(0, 150, 0));

        infoPanel.add(brandLabel);
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
        mainPanel.add(new JLabel("Choose Size and Color:"), gbc);

        gbc.gridy = 1;

        // Membuat class internal khusus untuk memanipulasi teks tampilan di Dropdown UI
        class VariantOption {
            Variant variant;
            int availableStock;

            VariantOption(Variant variant, int availableStock) {
                this.variant = variant;
                this.availableStock = availableStock;
            }

            @Override
            public String toString() {
                return variant.getSize() + " | " + variant.getColor() + " (Available: " + availableStock + ")";
            }
        }

        JComboBox<VariantOption> variantDropdown = new JComboBox<>();
        List<Variant> variants = storeController.getProductVariants(product.getId());
        List<CartItem> cart = parentFrame.getFloatingCart();

        boolean hasStock = false;

        // Kalkulasi stok realtime (Database - Keranjang)
        for (Variant v : variants) {
            int amountAlreadyInCart = 0;
            for (CartItem item : cart) {
                if (item.getVariantId() == v.getId()) {
                    amountAlreadyInCart = item.getQuantity();
                    break;
                }
            }

            int available = v.getStock() - amountAlreadyInCart;
            // Hanya tampilkan opsi di dropdown jika stok yang tersedia masih lebih dari 0
            if (available > 0) {
                variantDropdown.addItem(new VariantOption(v, available));
                hasStock = true;
            }
        }

        // Blokir jika semua varian habis atau sudah masuk keranjang semua
        if (!hasStock) {
            JOptionPane.showMessageDialog(parentFrame, "Sorry, all options for this product are currently out of stock or already maximized in your cart!");
            return;
        }

        mainPanel.add(variantDropdown, gbc);

        gbc.gridy = 2;
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(new JLabel("Quantity:  "));

        // Ambil nilai maksimal stok dari opsi pertama yang terpilih untuk mengatur batas atas Spinner
        VariantOption firstOption = (VariantOption) variantDropdown.getSelectedItem();
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, firstOption.availableStock, 1));
        qtyPanel.add(quantitySpinner);
        mainPanel.add(qtyPanel, gbc);

        // Listener agar nilai maksimal pada Spinner otomatis berubah saat warna/ukuran diganti
        variantDropdown.addActionListener(e -> {
            VariantOption selected = (VariantOption) variantDropdown.getSelectedItem();
            if (selected != null) {
                int currentVal = (Integer) quantitySpinner.getValue();
                int max = selected.availableStock;
                int newVal = Math.min(currentVal, max); // Cegah nilai melebihi batas maksimal yang baru
                quantitySpinner.setModel(new SpinnerNumberModel(newVal, 1, max, 1));
            }
        });

        gbc.gridy = 3;
        JButton confirmButton = new JButton("Confirm Add to Cart");
        confirmButton.setBackground(new Color(50, 200, 100));
        confirmButton.setForeground(Color.WHITE);

        confirmButton.addActionListener(e -> {
            VariantOption selectedOption = (VariantOption) variantDropdown.getSelectedItem();
            int selectedQty = (Integer) quantitySpinner.getValue();

            if (selectedOption != null) {
                Variant selectedVariant = selectedOption.variant;
                int amountAlreadyInCart = 0;
                CartItem existingCartItem = null;

                for (CartItem item : cart) {
                    if (item.getVariantId() == selectedVariant.getId()) {
                        amountAlreadyInCart = item.getQuantity();
                        existingCartItem = item;
                        break;
                    }
                }

                // Validasi keamanan akhir sebelum memasukkan data ke keranjang
                if ((selectedQty + amountAlreadyInCart) > selectedVariant.getStock()) {
                    JOptionPane.showMessageDialog(dialog, "Stock limited. Only " + selectedOption.availableStock + " more left.", "Stock Error", JOptionPane.WARNING_MESSAGE);
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