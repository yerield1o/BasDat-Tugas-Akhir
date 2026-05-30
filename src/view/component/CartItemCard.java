package view.component;

import model.CartItem;
import util.ImageLoader;
import view.dashboard.CartPanel;
import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class CartItemCard extends JPanel {

    public CartItemCard(int listIndex, CartItem item, CartPanel parentCartPanel) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 0, 10);

        gbc.gridx = 0; gbc.weightx = 0;
        JLabel imgLabel = new JLabel(ImageLoader.scaleImage("pictures/placeholder.png", 80, true));
        add(imgLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1));
        detailsPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(item.getProductName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel variantLabel = new JLabel(item.getVariantInfo());
        variantLabel.setForeground(Color.GRAY);
        JLabel priceLabel = new JLabel(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(item.getPrice()));
        priceLabel.setForeground(new Color(0, 150, 0));

        detailsPanel.add(nameLabel);
        detailsPanel.add(variantLabel);
        detailsPanel.add(priceLabel);
        add(detailsPanel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        qtyPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setMargin(new Insets(2, 6, 2, 6));
        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton plusBtn = new JButton("+");
        plusBtn.setMargin(new Insets(2, 6, 2, 6));

        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                parentCartPanel.refreshCartData();
            }
        });

        plusBtn.addActionListener(e -> {
            if (item.getQuantity() < item.getMaxStock()) {
                item.setQuantity(item.getQuantity() + 1);
                parentCartPanel.refreshCartData();
            } else {
                JOptionPane.showMessageDialog(this, "Stock Limit Reached!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);
        add(qtyPanel, gbc);

        gbc.gridx = 3;
        JButton removeBtn = new JButton("Remove");
        removeBtn.setBackground(new Color(220, 50, 50));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.addActionListener(e -> parentCartPanel.removeCartItem(listIndex));
        add(removeBtn, gbc);
    }
}