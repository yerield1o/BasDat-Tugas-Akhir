package view.dashboard;

import controller.CartController;
import model.CartItem;
import model.Courier;
import view.component.CartItemCard;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartPanel extends JPanel {
    private DashboardFrame parentFrame;
    private CartController cartController;

    private JPanel cartItemsContainer;
    private JTextArea billTextArea;
    private JLabel cartTotalLabel;

    public CartPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.cartController = new CartController();

        setLayout(new BorderLayout(20, 0));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // LEFT PANEL: Items
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);
        JLabel cartTitle = new JLabel("Shopping Cart");
        cartTitle.setFont(new Font("Arial", Font.BOLD, 28));

        JButton removeAllBtn = new JButton("Remove All");
        removeAllBtn.setBackground(new Color(220, 50, 50));
        removeAllBtn.setForeground(Color.WHITE);
        removeAllBtn.setFocusPainted(false);
        removeAllBtn.addActionListener(e -> {
            parentFrame.getFloatingCart().clear();
            refreshCartData();
        });

        leftHeader.add(cartTitle, BorderLayout.WEST);
        leftHeader.add(removeAllBtn, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        cartItemsContainer = new JPanel();
        cartItemsContainer.setLayout(new BoxLayout(cartItemsContainer, BoxLayout.Y_AXIS));
        cartItemsContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(cartItemsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);

        // RIGHT PANEL: Bill
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 22));
        rightPanel.add(summaryTitle, BorderLayout.NORTH);

        billTextArea = new JTextArea();
        billTextArea.setEditable(false);
        billTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        rightPanel.add(new JScrollPane(billTextArea), BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new BorderLayout(0, 15));
        checkoutPanel.setOpaque(false);
        cartTotalLabel = new JLabel("Total: Rp 0", SwingConstants.RIGHT);
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        checkoutPanel.add(cartTotalLabel, BorderLayout.NORTH);

        JButton checkoutBtn = new JButton("Continue to Purchase");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutBtn.setBackground(new Color(50, 200, 100));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setPreferredSize(new Dimension(0, 50));
        checkoutBtn.addActionListener(e -> showCheckoutPopUp());
        checkoutPanel.add(checkoutBtn, BorderLayout.SOUTH);

        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);
    }

    public void removeCartItem(int index) {
        parentFrame.getFloatingCart().remove(index);
        refreshCartData();
    }

    public void refreshCartData() {
        cartItemsContainer.removeAll();
        StringBuilder billText = new StringBuilder();
        double grandTotal = 0.0;
        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        List<CartItem> cart = parentFrame.getFloatingCart();

        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            double subtotal = item.getPrice() * item.getQuantity();
            grandTotal += subtotal;

            cartItemsContainer.add(new CartItemCard(i, item, this));
            billText.append(item.getProductName()).append(" (").append(item.getVariantInfo()).append(")\n");
            billText.append(item.getQuantity()).append(" x ").append(rpFormat.format(item.getPrice())).append("\n");
            billText.append("Subtotal: ").append(rpFormat.format(subtotal)).append("\n\n");
        }

        billTextArea.setText(billText.toString());
        cartTotalLabel.setText("Total: " + rpFormat.format(grandTotal));
        cartItemsContainer.revalidate();
        cartItemsContainer.repaint();
    }

    private void showCheckoutPopUp() {
        List<CartItem> cart = parentFrame.getFloatingCart();
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "Checkout Confirmation", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        mainPanel.add(new JLabel("Review Your Items:"), gbc);

        gbc.gridy = 1;
        JTextArea summaryArea = new JTextArea(8, 30);
        summaryArea.setEditable(false);
        double grandTotal = 0;
        for (CartItem item : cart) {
            summaryArea.append("- " + item.getQuantity() + "x " + item.getProductName() + "\n");
            grandTotal += (item.getPrice() * item.getQuantity());
        }
        mainPanel.add(new JScrollPane(summaryArea), gbc);

        gbc.gridy = 2; mainPanel.add(new JLabel("Select Courier:"), gbc);
        gbc.gridy = 3;
        JComboBox<Courier> courierDropdown = new JComboBox<>();
        List<Courier> couriers = cartController.getCouriers();
        for (Courier c : couriers) courierDropdown.addItem(c);
        mainPanel.add(courierDropdown, gbc);

        gbc.gridy = 4; mainPanel.add(new JLabel("Select Payment Method:"), gbc);
        gbc.gridy = 5;
        JComboBox<String> paymentDropdown = new JComboBox<>(new String[]{"Cash On Delivery", "Online Payment"});
        mainPanel.add(paymentDropdown, gbc);

        gbc.gridy = 6;
        JButton confirmOrderBtn = new JButton("Confirm Order");
        confirmOrderBtn.setBackground(new Color(50, 200, 100));
        confirmOrderBtn.setForeground(Color.WHITE);
        final double finalTotal = grandTotal;

        confirmOrderBtn.addActionListener(e -> {
            Courier selectedCourier = (Courier) courierDropdown.getSelectedItem();
            String selectedPayment = (String) paymentDropdown.getSelectedItem();

            if (selectedCourier != null) {
                boolean success = cartController.processCheckoutTransaction(
                        parentFrame.getLoggedInUser(), finalTotal, selectedCourier.getId(), selectedPayment, cart);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Purchase Successful!");
                    dialog.dispose();
                    cart.clear();
                    refreshCartData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Transaction failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainPanel.add(confirmOrderBtn, gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}